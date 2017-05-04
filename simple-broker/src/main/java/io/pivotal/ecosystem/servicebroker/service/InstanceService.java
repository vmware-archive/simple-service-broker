/**
 Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.

 This program and the accompanying materials are made available under
 the terms of the under the Apache License, Version 2.0 (the "License”);
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package io.pivotal.ecosystem.servicebroker.service;

import io.pivotal.ecosystem.servicebroker.model.LastOperation;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceDefinitionDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstanceService implements ServiceInstanceService {

    public static final String OBJECT_ID = "Instance";

    public InstanceService(CatalogService catalogService, BrokeredService brokeredService, RedisTemplate<String, ServiceInstance> instanceTemplate) {
        this.catalogService = catalogService;
        this.brokeredService = brokeredService;
        this.instanceTemplate = instanceTemplate;
    }

    private CatalogService catalogService;

    private BrokeredService brokeredService;

    private RedisTemplate<String, ServiceInstance> instanceTemplate;

    ServiceInstance getServiceInstance(String id) {
        if (id == null || getInstance(id) == null) {
            log.warn("service instance with id: " + id + " not found!");
            return null;
        }

        return getInstance(id);
    }

    @Override
    public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {

        try {
            if (getInstance(request.getServiceInstanceId()) != null) {
                throw new ServiceInstanceExistsException(request.getServiceInstanceId(), request.getServiceInstanceId());
            }
        } catch (ServiceInstanceDoesNotExistException e) {
            //ok, don't have this instance, keep going
        }

        ServiceDefinition sd = catalogService.getServiceDefinition(request.getServiceDefinitionId());

        if (sd == null) {
            throw new ServiceDefinitionDoesNotExistException(request.getServiceDefinitionId());
        }

        log.info("creating service instance: " + request.getServiceInstanceId() + " service definition: " + request.getServiceDefinitionId());
        ServiceInstance instance = new ServiceInstance(request);
        brokeredService.createInstance(instance);
        saveInstance(instance);

        log.info("registered service instance: " + request.getServiceInstanceId());
        return instance.getCreateResponse();
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest getLastServiceOperationRequest) {
        //TODO deal with Async
        return null;
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {

        log.info("starting service instance delete: " + request.getServiceInstanceId());
        ServiceInstance instance = getInstance(request.getServiceInstanceId());
        brokeredService.deleteInstance(instance);
        deleteInstance(instance);

        log.info("deleted service instance: " + request.getServiceInstanceId());
        return instance.getDeleteResponse();
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
        log.info("starting service instance update: " + request.getServiceInstanceId());
        ServiceInstance originalInstance = getInstance(request.getServiceInstanceId());
        ServiceInstance updatedInstance = new ServiceInstance(request);

        originalInstance.setServiceId(updatedInstance.getServiceId());
        originalInstance.setPlanId(updatedInstance.getPlanId());
        originalInstance.getParameters().putAll(updatedInstance.getParameters());

        brokeredService.updateInstance(originalInstance);
        saveInstance(originalInstance);

        log.info("updated service instance: " + request.getServiceInstanceId());
        return originalInstance.getUpdateResponse();
    }

    private ServiceInstance getInstance(String id) throws ServiceBrokerException {
        ServiceInstance instance = (ServiceInstance) instanceTemplate.opsForHash().get(OBJECT_ID, id);

        //if this is not an async broker, we are done
        if( ! brokeredService.isAsync()) {
            return instance;
        }

        // check the last operation
        LastOperation lo = instance.getLastOperation();
        if (lo == null || lo.getState() == null) {
            log.error("ServiceInstance: " + id + " has no last operation.");
            deleteInstance(instance);
            return null;
        }

        // if the instance is not in progress just return it.
        if (! instance.inProgress()) {
            return instance;
        }

        // if still in progress, let's check up on things...
        String currentRequestId = lo.getDescription();
        if (currentRequestId == null) {
            log.error("ServiceInstance: " + id + " last operation has no id.");
            deleteInstance(instance);
            return null;
        }

        log.info("service instance id: " + id + " request id: "
                + currentRequestId + " is in state: " + lo.getState());

        log.info("checking on status of request id: " + currentRequestId);
        OperationState currentState = null;
        try {
            currentState = brokeredService.getServiceStatus(instance);
            log.info("request: " + id + " status is: " + currentState.getValue());
        } catch (ServiceBrokerException e) {
            log.error("unable to get status of request: " + id, e);
            throw e;
        }

        lo.setState(currentState);
        instance.setLastOperation(lo);

        // if this is a delete request and was successful, remove the instance
        if (instance.isCurrentOperationSuccessful()
                && instance.isCurrentOperationDelete()) {
            return deleteInstance(instance);
        }

        return saveInstance(instance);
    }

    private ServiceInstance deleteInstance(ServiceInstance instance) {
        log.info("deleting service instance from repo: " + instance.getId());
        instanceTemplate.opsForHash().delete(OBJECT_ID, instance.getId());
        return instance;
    }

    private ServiceInstance saveInstance(io.pivotal.ecosystem.servicebroker.model.ServiceInstance instance) {
        log.info("saving service instance to repo: " + instance.getId());
        instanceTemplate.opsForHash().put(OBJECT_ID, instance.getId(), instance);
        return instance;
    }
}