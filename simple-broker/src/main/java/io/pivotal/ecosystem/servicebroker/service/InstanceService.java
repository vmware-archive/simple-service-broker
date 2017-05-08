/**
 * Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.servicebroker.service;

import io.pivotal.ecosystem.servicebroker.model.LastOperation;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerAsyncRequiredException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceDefinitionDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstanceService implements ServiceInstanceService {

    public InstanceService(CatalogService catalogService, BrokeredService brokeredService, ServiceInstanceRepository serviceInstanceRepository) {
        this.catalogService = catalogService;
        this.brokeredService = brokeredService;
        this.serviceInstanceRepository = serviceInstanceRepository;
    }

    private CatalogService catalogService;

    private BrokeredService brokeredService;

    private ServiceInstanceRepository serviceInstanceRepository;

    @Override
    public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
        if (serviceInstanceRepository.findOne(request.getServiceInstanceId()) != null) {
            throw new ServiceInstanceExistsException(request.getServiceInstanceId(), request.getServiceDefinitionId());
        }

        //make sure that if this is an async broker this is an async request
        if (brokeredService.isAsync() && !request.isAsyncAccepted()) {
            throw new ServiceBrokerAsyncRequiredException("This broker only accepts async requests.");
        }

        ServiceDefinition sd = catalogService.getServiceDefinition(request.getServiceDefinitionId());
        if (sd == null) {
            throw new ServiceDefinitionDoesNotExistException(request.getServiceDefinitionId());
        }

        log.info("creating service instance: " + request.getServiceInstanceId() + " service definition: " + request.getServiceDefinitionId());
        ServiceInstance instance = new ServiceInstance(request);
        if (brokeredService.isAsync()) {
            instance.setAcceptsIncomplete(true);
        } else {
            instance.setAcceptsIncomplete(false);
        }

        brokeredService.createInstance(instance);
        if (brokeredService.isAsync()) {
            instance.setLastOperation(new LastOperation(OperationState.IN_PROGRESS, request.getServiceInstanceId(), false));
        } else {
            instance.setLastOperation(new LastOperation(OperationState.SUCCEEDED, request.getServiceInstanceId(), false));
        }

        saveInstance(instance);

        log.info("registered service instance: " + request.getServiceInstanceId());
        return instance.getCreateResponse();
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest getLastServiceOperationRequest) {
        return getServiceInstance(getLastServiceOperationRequest.getServiceInstanceId()).getLastOperation().toResponse();
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {

        log.info("starting service instance delete: " + request.getServiceInstanceId());
        ServiceInstance instance = getServiceInstance(request.getServiceInstanceId());
        brokeredService.deleteInstance(instance);
        deleteInstance(instance);

        log.info("deleted service instance: " + request.getServiceInstanceId());
        return instance.getDeleteResponse();
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
        log.info("starting service instance update: " + request.getServiceInstanceId());
        ServiceInstance originalInstance = getServiceInstance(request.getServiceInstanceId());
        ServiceInstance updatedInstance = new ServiceInstance(request);

        originalInstance.setServiceId(updatedInstance.getServiceId());
        originalInstance.setPlanId(updatedInstance.getPlanId());
        originalInstance.getParameters().putAll(updatedInstance.getParameters());

        brokeredService.updateInstance(originalInstance);
        saveInstance(originalInstance);

        log.info("updated service instance: " + request.getServiceInstanceId());
        return originalInstance.getUpdateResponse();
    }

    public ServiceInstance getServiceInstance(String id) throws ServiceBrokerException {
        ServiceInstance instance = serviceInstanceRepository.findOne(id);

        //if this is not an async broker, we are done
        if (!brokeredService.isAsync()) {
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
        if (!instance.inProgress()) {
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
        serviceInstanceRepository.delete(instance.getId());
        return instance;
    }

    private ServiceInstance saveInstance(io.pivotal.ecosystem.servicebroker.model.ServiceInstance instance) {
        log.info("saving service instance to repo: " + instance.getId());
        serviceInstanceRepository.save(instance);
        return instance;
    }
}