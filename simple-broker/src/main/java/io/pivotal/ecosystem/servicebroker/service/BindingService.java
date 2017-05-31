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
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.*;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class BindingService implements ServiceInstanceBindingService {

    public BindingService(InstanceService instanceService, BrokeredService brokeredService, ServiceBindingRepository serviceBindingRepository) {
        this.instanceService = instanceService;
        this.brokeredService = brokeredService;
        this.serviceBindingRepository = serviceBindingRepository;
    }

    private InstanceService instanceService;

    private BrokeredService brokeredService;

    private ServiceBindingRepository serviceBindingRepository;

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {

        ServiceInstance instance = instanceService.getServiceInstance(request.getServiceInstanceId());

        if (getBinding(request.getBindingId()) != null) {
            throw new ServiceInstanceBindingExistsException(request.getServiceInstanceId(), request.getBindingId());
        }

        log.info("creating binding for service instance: " + request.getServiceInstanceId() + " service: " + request.getServiceInstanceId());
        ServiceBinding binding = new ServiceBinding(request);

        try {
            binding.setLastOperation(brokeredService.createBinding(instance, binding));
            responseSanity(binding.getLastOperation());
        } catch (Throwable t) {
            binding.setLastOperation(new LastOperation(Operation.CREATE, OperationState.FAILED, t.getMessage()));
            binding.setDeleted(true);
            serviceBindingRepository.save(binding);
            throw new ServiceBrokerException("error creating binding", t);
        }

        if (OperationState.FAILED.equals(binding.getLastOperation().getState())) {
            binding.setDeleted(true);
            serviceBindingRepository.save(binding);
            throw new ServiceBrokerException("error creating binding: " + binding.getLastOperation());
        }

        Map<String, Object> creds = brokeredService.getCredentials(instance, binding);
        binding.getCredentials().putAll(creds);

        log.info("saving binding: " + request.getBindingId());
        serviceBindingRepository.save(binding);

        return binding.getCreateResponse();
    }

    @Override
    public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
        ServiceBinding binding = getBinding(request.getBindingId());
        if (binding == null || binding.isDeleted()) {
            throw new ServiceInstanceBindingDoesNotExistException(request.getBindingId());
        }

        String serviceInstanceId = request.getServiceInstanceId();
        ServiceInstance si = instanceService.getServiceInstance(serviceInstanceId);

        try {
            binding.setLastOperation(brokeredService.deleteBinding(si, binding));
            responseSanity(binding.getLastOperation());
        } catch (Throwable t) {
            binding.setLastOperation(new LastOperation(Operation.DELETE, OperationState.FAILED, t.getMessage()));
            serviceBindingRepository.save(binding);
            throw new ServiceBrokerException("error deleting binding", t);
        }

        log.info("deleting binding for service instance: " + request.getBindingId() + " service instance: " + request.getServiceInstanceId());
        binding.setDeleted(true);
        serviceBindingRepository.save(binding);
    }

    private ServiceBinding getBinding(String id) {
        return serviceBindingRepository.findOne(id);
    }

    private void responseSanity(LastOperation lastOperation) {
        if (OperationState.IN_PROGRESS.equals(lastOperation.getState())) {
            throw new ServiceBrokerException("service returned in progress, but bind operations are synchronous.");
        }
    }
}