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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerAsyncRequiredException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceServiceTest {

    private static final String ID = "deleteme";

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    private DefaultServiceImpl mockDefaultServiceImpl;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private InstanceService instanceServiceSync() {
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);
        return new InstanceService(catalogService, mockDefaultServiceImpl, serviceInstanceRepository);
    }

    @Before
    public void setUp() {
        if (serviceInstanceRepository.findOne(ID) != null) {
            serviceInstanceRepository.delete(ID);
        }
    }

    @Test
    public void testHappyLifeCycle() throws ServiceBrokerException {
        InstanceService service = instanceServiceSync();

        //grab an id to use throughout
        String id = UUID.randomUUID().toString();

        assertNull(serviceInstanceRepository.findOne(id));

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        CreateServiceInstanceResponse csir = service.createServiceInstance(TestConfig.createRequest(id, false));
        assertNotNull(csir);
        assertNotNull(serviceInstanceRepository.findOne(id));

        ServiceInstance si = service.getServiceInstance(id);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        GetLastServiceOperationResponse glsor = service.getLastOperation(TestConfig.lastOperationRequest(id));
        assertNotNull(glsor);
        assertEquals(OperationState.SUCCEEDED, glsor.getState());

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        UpdateServiceInstanceResponse usir = service.updateServiceInstance(TestConfig.updateRequest(id, false));
        assertNotNull(usir);

        si = service.getServiceInstance(id);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        glsor = service.getLastOperation(TestConfig.lastOperationRequest(id));
        assertNotNull(glsor);
        assertEquals(OperationState.SUCCEEDED, glsor.getState());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        DeleteServiceInstanceResponse dsir = service.deleteServiceInstance(TestConfig.deleteRequest(id, false));
        assertNotNull(dsir);

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        glsor = service.getLastOperation(TestConfig.lastOperationRequest(id));
        assertNotNull(glsor);
        assertEquals(OperationState.SUCCEEDED, glsor.getState());

        assertNull(serviceInstanceRepository.findOne(id));
    }

    @Test
    public void testAsyncRequest() {
        InstanceService service = instanceServiceSync();
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.createServiceInstance(TestConfig.createRequest(ID, true));

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.updateServiceInstance(TestConfig.updateRequest(ID, true));

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testDuplicateCreate() {
        InstanceService service = instanceServiceSync();

        if (serviceInstanceRepository.findOne(ID) != null) {
            serviceInstanceRepository.delete(ID);
        }

        service.createServiceInstance(TestConfig.createRequest(ID, false));

        exception.expect(ServiceInstanceExistsException.class);
        service.createServiceInstance(TestConfig.createRequest(ID, false));

        service.deleteServiceInstance(TestConfig.deleteRequest(ID, false));
    }

}