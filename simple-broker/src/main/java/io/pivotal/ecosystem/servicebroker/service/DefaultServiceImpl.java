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

import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.OperationState;

import java.util.HashMap;
import java.util.Map;

public class DefaultServiceImpl implements BrokeredService {

    @Override
    public void createInstance(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public void deleteInstance(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public void updateInstance(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public void createBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {

    }

    @Override
    public void deleteBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {

    }

    @Override
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        return new HashMap<>();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public OperationState getServiceStatus(ServiceInstance instance) {
        return OperationState.SUCCEEDED;
    }
}
