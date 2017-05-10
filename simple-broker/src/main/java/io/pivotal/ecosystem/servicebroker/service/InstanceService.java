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
import io.pivotal.ecosystem.servicebroker.model.Operation;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.*;
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

        //make sure that if this is an sync broker this is not an async request
        if (!brokeredService.isAsync() && request.isAsyncAccepted()) {
            throw new ServiceBrokerAsyncRequiredException("This broker only accepts synchronous requests.");
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

        instance.setLastOperation(brokeredService.createInstance(instance));
        saveInstance(instance);

        log.info("registered service instance: " + request.getServiceInstanceId());
        return instance.getCreateResponse();
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest getLastServiceOperationRequest) {
        return serviceInstanceRepository.findOne(getLastServiceOperationRequest.getServiceInstanceId()).getLastOperation().toResponse();
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
        if (serviceInstanceRepository.findOne(request.getServiceInstanceId()) != null && serviceInstanceRepository.findOne(request.getServiceInstanceId()).isDeleted()) {
            throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
        }

        log.info("starting service instance delete: " + request.getServiceInstanceId());
        ServiceInstance instance = getServiceInstance(request.getServiceInstanceId());

        //do not accept an delete request if the last operation is still in process
        if (instance.getLastOperation().getState().equals(OperationState.IN_PROGRESS)) {
            throw new ServiceBrokerException(instance.getLastOperation().toString() + " is still in process.");
        }

        LastOperation lo = brokeredService.deleteInstance(instance);
        instance.setLastOperation(lo);
        saveInstance(instance);

        if (!brokeredService.isAsync() || OperationState.SUCCEEDED.equals(lo.getState())) {
            deleteInstance(instance);
        }

        return instance.getDeleteResponse();
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
        if (serviceInstanceRepository.findOne(request.getServiceInstanceId()) != null && serviceInstanceRepository.findOne(request.getServiceInstanceId()).isDeleted()) {
            throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
        }

        log.info("starting service instance update: " + request.getServiceInstanceId());
        ServiceInstance originalInstance = getServiceInstance(request.getServiceInstanceId());

        //do not accept an update request if the last operation is still in process
        if (originalInstance.getLastOperation().getState().equals(OperationState.IN_PROGRESS)) {
            throw new ServiceBrokerException(originalInstance.getLastOperation().toString() + " is still in process.");
        }

        ServiceInstance updatedInstance = new ServiceInstance(request);
        originalInstance.getLastOperation().setOperation(Operation.UPDATE);
        originalInstance.getLastOperation().setState(OperationState.IN_PROGRESS);
        updatedInstance.setLastOperation(originalInstance.getLastOperation());

        LastOperation lo = brokeredService.updateInstance(updatedInstance);

        if (OperationState.FAILED.equals(lo.getState())) {
            log.info("update failed: " + request.getServiceInstanceId());
            originalInstance.getLastOperation().setState(OperationState.FAILED);
            saveInstance(originalInstance);
            return originalInstance.getUpdateResponse();
        }

        log.info("updated service instance: " + request.getServiceInstanceId());
        updatedInstance.setLastOperation(lo);
        saveInstance(updatedInstance);
        return updatedInstance.getUpdateResponse();
    }

    ServiceInstance getServiceInstance(String id) throws ServiceBrokerException {
        ServiceInstance instance = serviceInstanceRepository.findOne(id);

        //if this is not an async broker, we can just return the instance
        if (!brokeredService.isAsync()) {
            return instance;
        }

        //async then...

        //if last state is not in progress we can return (no need to check up on progress)
        if (!OperationState.IN_PROGRESS.equals(instance.getLastOperation().getState())) {
            return instance;
        }

        //let's check up on things
        log.info("checking on status of request id: " + instance.getId());
        try {
            instance.setLastOperation(brokeredService.getServiceStatus(instance));
            log.info("request: " + id + " status is: " + instance.getLastOperation());
        } catch (ServiceBrokerException e) {
            log.error("unable to get status of request: " + id, e);
            instance.getLastOperation().setState(OperationState.FAILED);
        }

        //if state is failed remove the instance, but not updates (leave them as is).
        if (OperationState.FAILED.equals(instance.getLastOperation().getState()) && !Operation.UPDATE.equals(instance.getLastOperation().getOperation())) {
            return deleteInstance(instance);
        }

        // if this is a delete request and was successful, remove the instance
        if (instance.isCurrentOperationSuccessful()
                && instance.isCurrentOperationDelete()) {
            return deleteInstance(instance);
        }

        //otherwise save updated instance and return
        return saveInstance(instance);
    }

    private ServiceInstance deleteInstance(ServiceInstance instance) {
        log.info("deleting service instance from repo: " + instance.getId());
        instance.setDeleted(true);
        serviceInstanceRepository.save(instance);
        return instance;
    }

    private ServiceInstance saveInstance(io.pivotal.ecosystem.servicebroker.model.ServiceInstance instance) {
        log.info("saving service instance to repo: " + instance.getId());
        serviceInstanceRepository.save(instance);
        return instance;
    }
}