/*
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
        requestSanity(request);

        if (serviceInstanceRepository.findOne(request.getServiceInstanceId()) != null) {
            throw new ServiceInstanceExistsException(request.getServiceInstanceId(), request.getServiceDefinitionId());
        }

        ServiceDefinition sd = catalogService.getServiceDefinition(request.getServiceDefinitionId());
        if (sd == null) {
            throw new ServiceDefinitionDoesNotExistException(request.getServiceDefinitionId());
        }

        log.info("creating service instance: " + request.getServiceInstanceId() + " service definition: " + request.getServiceDefinitionId());
        ServiceInstance instance = new ServiceInstance(request);
        if (brokeredService.isAsync() && request.isAsyncAccepted()) {
            instance.setAcceptsIncomplete(true);
        } else {
            instance.setAcceptsIncomplete(false);
        }

        try {
            instance.setLastOperation(brokeredService.createInstance(instance));
        } catch (Throwable t) {
            log.error("error creating instance: " + request.getServiceInstanceId(), t);
            instance.setLastOperation(new LastOperation(Operation.CREATE, OperationState.FAILED, t.getMessage()));
        }

        responseSanity(instance);

        if (instance.isFailed()) {
            instance.setDeleted(true);
        }

        saveInstance(instance);

        log.info("registered service instance: " + request.getServiceInstanceId());
        return instance.getCreateResponse();
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest getLastServiceOperationRequest) {
        ServiceInstance instance = serviceInstanceRepository.findOne(getLastServiceOperationRequest.getServiceInstanceId());
        if (instance == null) {
            throw new ServiceInstanceDoesNotExistException(getLastServiceOperationRequest.getServiceInstanceId());
        }

        //if this is not an async broker, we can just return
        if (!brokeredService.isAsync()) {
            return instance.getLastOperation().toResponse();
        }

        //if last state is not in progress no need to update last operation..
        if (!instance.isInProgress()) {
            return instance.getLastOperation().toResponse();
        }

        log.info("checking on status of request id: " + instance.getId());
        try {
            instance.setLastOperation(brokeredService.lastOperation(instance));
        } catch (Throwable t) {
            log.error("error checking status of instance: " + instance.getId(), t);
            instance.getLastOperation().setState(OperationState.FAILED);
            instance.getLastOperation().setDescription(t.getMessage());
            return instance.getLastOperation().toResponse();
        }

        responseSanity(instance);
        log.info("request: " + getLastServiceOperationRequest.getServiceInstanceId() + " status is: " + instance.getLastOperation());

        //if a create failed, delete the instance. Don't delete failed updates or failed deletes (user can try again later)
        if (instance.isFailed() && instance.isCreate()) {
            return deleteInstance(instance).getLastOperation().toResponse();
        }

        // if this is a delete request and was successful, delete the instance
        if (instance.isSuccessful() && instance.isDelete()) {
            return deleteInstance(instance).getLastOperation().toResponse();
        }

        //otherwise save updated instance and return
        return saveInstance(instance).getLastOperation().toResponse();
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
        requestSanity(request);

        ServiceInstance instance = serviceInstanceRepository.findOne(request.getServiceInstanceId());
        if (instance == null || instance.isDeleted()) {
            throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
        }

        //do not accept an delete request if the last operation is still in process
        if (instance.isInProgress()) {
            throw new ServiceBrokerException(instance.getLastOperation().toString() + " is still in process.");
        }

        log.info("starting service instance delete: " + request.getServiceInstanceId());
        try {
            instance.setLastOperation(brokeredService.deleteInstance(instance));
        } catch (Throwable t) {
            log.error("error deleting instance: " + request.getServiceInstanceId(), t);
            instance.setLastOperation(new LastOperation(Operation.DELETE, OperationState.FAILED, t.getMessage()));
        }

        responseSanity(instance);

        saveInstance(instance);

        if (instance.isSuccessful()) {
            deleteInstance(instance);
        }

        return instance.getDeleteResponse();
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
        requestSanity(request);

        ServiceInstance instance = serviceInstanceRepository.findOne(request.getServiceInstanceId());
        if (instance == null || instance.isDeleted()) {
            throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
        }

        //do not accept an update request if the last operation is still in process
        if (instance.isInProgress()) {
            throw new ServiceBrokerException(instance.getId());
        }

        ServiceInstance updatedInstance = new ServiceInstance(request);
        for (String key : instance.getParameters().keySet()) {
            if (!updatedInstance.getParameters().containsKey(key)) {
                updatedInstance.addParameter(key, instance.getParameters().get(key));
            }
        }

        try {
            updatedInstance.setLastOperation(brokeredService.updateInstance(updatedInstance));
        } catch (Throwable t) {
            log.error("error updating instance: " + updatedInstance.getId(), t);
            updatedInstance.setLastOperation(new LastOperation(Operation.UPDATE, OperationState.FAILED, t.getMessage()));
        }

        responseSanity(updatedInstance);

        if (updatedInstance.isFailed()) {
            log.info("update failed: " + request.getServiceInstanceId());
            instance.getLastOperation().setState(OperationState.FAILED);
            instance.getLastOperation().setDescription(updatedInstance.getLastOperation().getDescription());
            saveInstance(instance);
            return instance.getUpdateResponse();
        }

        log.info("updated service instance: " + request.getServiceInstanceId());
        saveInstance(updatedInstance);
        return updatedInstance.getUpdateResponse();
    }

    ServiceInstance getServiceInstance(String id) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findOne(id);
        if (serviceInstance == null || serviceInstance.isDeleted()) {
            throw new ServiceInstanceDoesNotExistException(id);
        }
        return serviceInstance;
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

    private void requestSanity(AsyncServiceInstanceRequest request) {
        if (brokeredService.isAsync() && !request.isAsyncAccepted()) {
            throw new ServiceBrokerAsyncRequiredException("This service plan requires client support for asynchronous service operations.");
        }
    }

    private void responseSanity(ServiceInstance instance) {
        if (instance.getLastOperation() == null) {
            throw new ServiceBrokerInvalidParametersException("no last operation on instance: " + instance);
        }

        if (instance.isInProgress() && !brokeredService.isAsync()) {
            throw new ServiceBrokerInvalidParametersException("This service plan requires client support for asynchronous service operations.");
        }
    }
}