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
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BindingServiceTest {

    private static final String ID = "deleteme";

    @Autowired
    private BindingService bindingService;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ServiceBindingRepository serviceBindingRepository;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    private DefaultServiceImpl mockDefaultServiceImpl;

    private InstanceService instanceService() {
        return new InstanceService(catalogService, mockDefaultServiceImpl, serviceInstanceRepository);
    }

    @Before
    public void setUp() {
        if (serviceBindingRepository.findOne(ID) != null) {
            serviceBindingRepository.delete(ID);
        }

        if (serviceInstanceRepository.findOne(ID) != null) {
            serviceInstanceRepository.delete(ID);
        }
    }

    @Test
    public void testBinding() throws ServiceBrokerException {
        InstanceService instanceService = instanceService();

        //nothing there to begin with
        when(mockDefaultServiceImpl.createInstance(any(io.pivotal.ecosystem.servicebroker.model.ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        assertNull(serviceInstanceRepository.findOne(ID));
        instanceService.createServiceInstance(TestConfig.createRequest(ID, false));

        assertNull(serviceBindingRepository.findOne(ID));

        CreateServiceInstanceAppBindingResponse cresp = (CreateServiceInstanceAppBindingResponse)
                bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID));

        assertNotNull(cresp);
        assertNotNull(cresp.getCredentials());

        assertNotNull(serviceBindingRepository.findOne(ID));

        bindingService.deleteServiceInstanceBinding(TestConfig.deleteBindingRequest(ID, ID));
        ServiceBinding sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertTrue(sb.isDeleted());

        when(mockDefaultServiceImpl.deleteInstance(any(io.pivotal.ecosystem.servicebroker.model.ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, false));
    }
}