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
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.*;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;
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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

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

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        instanceService().createServiceInstance(TestConfig.createRequest(ID, true));
    }

    @After
    public void cleanUp() {
        if (serviceBindingRepository.findOne(ID) != null) {
            serviceBindingRepository.delete(ID);
        }

        if (serviceInstanceRepository.findOne(ID) != null) {
            serviceInstanceRepository.delete(ID);
        }
    }

    @Test
    public void testThatInProgressCreateFails() {
        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.BIND, OperationState.IN_PROGRESS, "binding."));

        exception.expect(ServiceBrokerException.class);
        bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID));
    }

    @Test
    public void testThatInProgressDeleteFails() {
        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.BIND, OperationState.SUCCEEDED, "bound."));
        bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID));

        when(mockDefaultServiceImpl.deleteBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.IN_PROGRESS, "unbound."));

        exception.expect(ServiceBrokerException.class);
        bindingService.deleteServiceInstanceBinding(TestConfig.deleteBindingRequest(ID, ID));
    }

    @Test
    public void testThatBogusCreateIdsAreRejected() {
        exception.expect(ServiceInstanceDoesNotExistException.class);
        bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest("bogus", ID));
    }

    @Test
    public void testThatBogusDeleteIdsAreRejected() {
        exception.expect(ServiceInstanceBindingDoesNotExistException.class);
        bindingService.deleteServiceInstanceBinding(TestConfig.deleteBindingRequest(ID, "bogus"));
    }

    @Test
    public void testThatDeletedIdDeletesAreRejected() {
        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.BIND, OperationState.SUCCEEDED, "bound."));
        when(mockDefaultServiceImpl.deleteBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.UNBIND, OperationState.SUCCEEDED, "unbound."));
        bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID));
        bindingService.deleteServiceInstanceBinding(TestConfig.deleteBindingRequest(ID, ID));

        exception.expect(ServiceInstanceBindingDoesNotExistException.class);
        bindingService.deleteServiceInstanceBinding(TestConfig.deleteBindingRequest(ID, ID));
    }

    @Test
    public void testThatDuplicateIdCreatesAreRejected() {
        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.BIND, OperationState.SUCCEEDED, "bound."));
        bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID));

        exception.expect(ServiceInstanceBindingExistsException.class);
        bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID));
    }

    @Test
    public void testFailedRequests() {
        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.BIND, OperationState.FAILED, "bound."));
        exception.expect(ServiceBrokerException.class);

        bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID));
        ServiceBinding sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertEquals(OperationState.FAILED, sb.getLastOperation().getState());
        assertTrue(sb.isDeleted());

        serviceInstanceRepository.delete(ID);

        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.BIND, OperationState.SUCCEEDED, "bound."));
        assertNotNull(bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID)));
        sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertEquals(OperationState.FAILED, sb.getLastOperation().getState());
        assertFalse(sb.isDeleted());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UNBIND, OperationState.FAILED, null));
        bindingService.deleteServiceInstanceBinding(TestConfig.deleteBindingRequest(ID, ID));
        sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertEquals(OperationState.FAILED, sb.getLastOperation().getState());
        assertFalse(sb.isDeleted());
    }

    @Test
    public void testBrokerCreateException() {
        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenThrow(new RuntimeException("aaaagh!"));
        exception.expect(ServiceBrokerException.class);

        assertNotNull(bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID)));
        ServiceBinding sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertEquals(OperationState.FAILED, sb.getLastOperation().getState());
        assertTrue(sb.isDeleted());
    }

    @Test
    public void testBrokerOtherExceptions() {
        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.BIND, OperationState.SUCCEEDED, "bound."));
        assertNotNull(bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID)));
        ServiceBinding sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertEquals(OperationState.SUCCEEDED, sb.getLastOperation().getState());
        assertFalse(sb.isDeleted());

        when(mockDefaultServiceImpl.deleteBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenThrow(new RuntimeException("ooof!"));
        exception.expect(ServiceBrokerException.class);

        bindingService.deleteServiceInstanceBinding(TestConfig.deleteBindingRequest(ID, ID));
        sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertEquals(OperationState.FAILED, sb.getLastOperation().getState());
        assertFalse(sb.isDeleted());
    }

    @Test
    public void testHappyLifeCycle() throws ServiceBrokerException {
        when(mockDefaultServiceImpl.createBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.BIND, OperationState.SUCCEEDED, "bound."));
        assertNotNull(bindingService.createServiceInstanceBinding(TestConfig.createBindingRequest(ID, ID)));
        ServiceBinding sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertEquals(OperationState.SUCCEEDED, sb.getLastOperation().getState());
        assertFalse(sb.isDeleted());

        when(mockDefaultServiceImpl.deleteBinding(any(ServiceInstance.class), any(ServiceBinding.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "unbound."));
        bindingService.deleteServiceInstanceBinding(TestConfig.deleteBindingRequest(ID, ID));

        sb = serviceBindingRepository.findOne(ID);
        assertNotNull(sb);
        assertEquals(OperationState.SUCCEEDED, sb.getLastOperation().getState());
        assertTrue(sb.isDeleted());
    }
}