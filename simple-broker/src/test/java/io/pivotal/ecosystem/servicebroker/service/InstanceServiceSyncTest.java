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
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceServiceSyncTest {

    private static final String ID = "deleteme";

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    private DefaultServiceImpl mockDefaultServiceImpl;

    @Autowired
    private InstanceService instanceService;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private ServiceInstance serviceInstance;

    @Before
    public void setUp() {
        serviceInstance = TestConfig.getServiceInstance(ID, false);
        LastOperation lastOperation = new LastOperation(LastOperation.CREATE, LastOperation.SUCCEEDED, "created.");
        serviceInstance.setLastOperation(lastOperation);
        when(mockDefaultServiceImpl.createInstance(serviceInstance)).thenReturn(lastOperation);
        when(mockDefaultServiceImpl.lastOperation(serviceInstance)).thenReturn(lastOperation);
        when(serviceInstanceRepository.findOne(ID)).thenReturn(serviceInstance);

        when(serviceInstanceRepository.save(serviceInstance)).thenReturn(serviceInstance);
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);
    }

    @Test
    public void testThatInProgressCreateFails() {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.IN_PROGRESS, "creating."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        instanceService.createServiceInstance(TestConfig.createRequest("bogus", true));
    }

    @Test
    public void testThatInProgressUpdateFails() {
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.UPDATE, LastOperation.IN_PROGRESS, "updating."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true));
    }

    @Test
    public void testThatInProgressDeleteFails() {
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.IN_PROGRESS, "deleteing."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testThatBogusUpdateIdsAreRejected() {
        exception.expect(ServiceInstanceDoesNotExistException.class);
        instanceService.updateServiceInstance(TestConfig.updateRequest("bogus", true));
    }

    @Test
    public void testThatBogusDeleteIdsAreRejected() {
        exception.expect(ServiceInstanceDoesNotExistException.class);
        instanceService.updateServiceInstance(TestConfig.updateRequest("bogus", true));
    }

    @Test
    public void testThatDeletedIdUpdatesAreRejected() {
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, "deleted."));
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));

        exception.expect(ServiceInstanceDoesNotExistException.class);
        instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true));
    }

    @Test
    public void testThatDeletedIdDeletesAreRejected() {
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, "deleted."));
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));

        exception.expect(ServiceInstanceDoesNotExistException.class);
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testThatDuplicateIdCreatesAreRejected() {
        exception.expect(ServiceInstanceExistsException.class);
        instanceService.createServiceInstance(TestConfig.createRequest(ID, true));
    }

    @Test
    public void testFailedRequests() {
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.UPDATE, LastOperation.FAILED, null));
        assertNotNull(instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true)));
        assertEquals(LastOperation.FAILED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.FAILED, null));
        assertNotNull(instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        assertEquals(LastOperation.FAILED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());
    }

    @Test
    public void testBrokerOtherExceptions() {
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenThrow(new ServiceBrokerException("noooooo!"));
        assertNotNull(instanceService.updateServiceInstance(TestConfig.updateRequest(ID, false)));
        assertEquals(LastOperation.FAILED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenThrow(new ServiceBrokerException("ooof!"));
        assertNotNull(instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, false)));
        assertEquals(LastOperation.FAILED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());
    }

    @Test
    public void testLastOperation() {
        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());

        //failed create
        GetLastServiceOperationResponse glsoresp = instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertNotNull(glsoresp);
        assertEquals(OperationState.SUCCEEDED, glsoresp.getState());

        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        //failed delete
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.FAILED, "deleted."));
        assertNotNull(instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        assertNotNull(instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID)));
        assertEquals(LastOperation.FAILED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        //successful delete
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.FAILED, "deleted."));
        assertNotNull(instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        assertFalse(serviceInstance.isDeleted());

        //bogus id
        exception.expect(ServiceInstanceDoesNotExistException.class);
        instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest("bogus"));
    }

    @Test
    public void testHappyLifeCycle() throws ServiceBrokerException {
        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        //update instanceService
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.UPDATE, LastOperation.SUCCEEDED, "updating."));
        UpdateServiceInstanceResponse usir = instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true));
        assertNotNull(usir);

        instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        //delete instanceService
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, "deleting."));
        DeleteServiceInstanceResponse dsir = instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
        assertNotNull(dsir);

        instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());
        assertTrue(serviceInstance.isDeleted());
    }

    @Test
    public void testAsyncCreateResponse() {
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.IN_PROGRESS, "created."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        instanceService.createServiceInstance(TestConfig.createRequest("bogus", true));
    }

    @Test
    public void testOtherAsyncResponses() {
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.UPDATE, LastOperation.IN_PROGRESS, "updated."));
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.IN_PROGRESS, "deleted."));
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.IN_PROGRESS, "created."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true));
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }
}