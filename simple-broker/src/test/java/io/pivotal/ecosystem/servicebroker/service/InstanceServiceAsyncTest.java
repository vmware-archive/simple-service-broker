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
import org.springframework.cloud.servicebroker.exception.ServiceBrokerAsyncRequiredException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceServiceAsyncTest {

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
        serviceInstance = TestConfig.getServiceInstance(ID, true);
        LastOperation lastOperation = new LastOperation(LastOperation.CREATE, LastOperation.IN_PROGRESS, "creating.");
        serviceInstance.setLastOperation(lastOperation);
        when(mockDefaultServiceImpl.createInstance(serviceInstance)).thenReturn(lastOperation);
        when(mockDefaultServiceImpl.lastOperation(serviceInstance)).thenReturn(lastOperation);
        when(serviceInstanceRepository.findOne(ID)).thenReturn(serviceInstance);

        when(serviceInstanceRepository.save(serviceInstance)).thenReturn(serviceInstance);
        when(mockDefaultServiceImpl.isAsync()).thenReturn(true);
    }

    @Test
    public void testHappyLifeCycle() throws ServiceBrokerException {
        //create an instance, already "done" in setup()
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.SUCCEEDED, "created."));
        instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        //update instanceService
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.UPDATE, LastOperation.IN_PROGRESS, "updating."));
        UpdateServiceInstanceResponse usir = instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true));
        assertNotNull(usir);

        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.UPDATE, LastOperation.SUCCEEDED, "updated."));
        instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        //delete instanceService
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.IN_PROGRESS, "deleting."));
        DeleteServiceInstanceResponse dsir = instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
        assertNotNull(dsir);

        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, "deleted."));
        instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertEquals(LastOperation.SUCCEEDED, serviceInstance.getLastOperation().getState());
        assertTrue(serviceInstance.isDeleted());
    }

    @Test
    public void testThatCreateSyncRequestsAreRejected() {
        exception.expect(ServiceBrokerAsyncRequiredException.class);
        instanceService.createServiceInstance(TestConfig.createRequest(ID, false));
    }

    @Test
    public void testThatUpdateSyncRequestsAreRejected() {
        exception.expect(ServiceBrokerAsyncRequiredException.class);
        instanceService.updateServiceInstance(TestConfig.updateRequest(ID, false));
    }

    @Test
    public void testThatDeleteSyncRequestsAreRejected() {
        exception.expect(ServiceBrokerAsyncRequiredException.class);
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, false));
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
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, "deleted."));

        serviceInstance.getLastOperation().setState(LastOperation.SUCCEEDED);
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));

        exception.expect(ServiceInstanceDoesNotExistException.class);
        instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true));
    }

    @Test
    public void testThatDeletedIdDeletesAreRejected() {
        serviceInstance.getLastOperation().setState(LastOperation.SUCCEEDED);
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, "deleted."));
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));

        exception.expect(ServiceInstanceDoesNotExistException.class);
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testThatDuplicateIdCreatesAreRejected() {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.SUCCEEDED, "created."));

        exception.expect(ServiceInstanceExistsException.class);
        instanceService.createServiceInstance(TestConfig.createRequest(ID, true));
    }

    @Test
    public void testFailedRequests() {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.FAILED, null));
        assertNotNull(instanceService.createServiceInstance(TestConfig.createRequest("bogus", true)));

        serviceInstance.getLastOperation().setState(LastOperation.SUCCEEDED);
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
    public void testBrokerCreateException() {
        ServiceInstance serviceInstance = TestConfig.getServiceInstance("bogus", false);
        LastOperation lastOperation = new LastOperation(LastOperation.CREATE, LastOperation.SUCCEEDED, "created.");
        serviceInstance.setLastOperation(lastOperation);
        when(mockDefaultServiceImpl.createInstance(serviceInstance)).thenReturn(lastOperation);

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenThrow(new ServiceBrokerException("aaaagh!"));
        CreateServiceInstanceResponse csir = instanceService.createServiceInstance(TestConfig.createRequest("bogus", true));
        assertNotNull(csir);
    }

    @Test
    public void testBrokerOtherExceptions() {
        serviceInstance.getLastOperation().setState(LastOperation.SUCCEEDED);
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenThrow(new ServiceBrokerException("noooooo!"));
        assertNotNull(instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(LastOperation.FAILED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenThrow(new ServiceBrokerException("ooof!"));
        assertNotNull(instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(LastOperation.FAILED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());
    }

    @Test
    public void testRequestsWhileStillInProgress() {
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.UPDATE, LastOperation.IN_PROGRESS, "updating."));
        exception.expect(ServiceBrokerException.class);
        instanceService.updateServiceInstance(TestConfig.updateRequest(ID, true));

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.IN_PROGRESS, "deleting."));
        exception.expect(ServiceBrokerException.class);
        instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testLastOperation() {

        //successful create
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.IN_PROGRESS, "creating."));
        assertNotNull(instanceService.createServiceInstance(TestConfig.createRequest("bogus", true)));

        //failed create
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.CREATE, LastOperation.FAILED, "creating."));
        GetLastServiceOperationResponse glsoresp = instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertNotNull(glsoresp);
        assertEquals(OperationState.FAILED, glsoresp.getState());

        assertEquals(LastOperation.FAILED, serviceInstance.getLastOperation().getState());
        assertTrue(serviceInstance.isDeleted());

        //failed delete
        serviceInstance.getLastOperation().setState(LastOperation.SUCCEEDED);
        serviceInstance.setDeleted(false);
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.IN_PROGRESS, "deleted."));
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.FAILED, "oops."));
        assertNotNull(instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        assertNotNull(instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest(ID)));
        assertEquals(LastOperation.FAILED, serviceInstance.getLastOperation().getState());
        assertFalse(serviceInstance.isDeleted());

        //successful delete
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.IN_PROGRESS, "deleted."));
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, "deleted."));
        assertNotNull(instanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        assertFalse(serviceInstance.isDeleted());

        //bogus id
        exception.expect(ServiceInstanceDoesNotExistException.class);
        instanceService.getLastOperation(TestConfig.getLastServiceOperationRequest("bogus"));
    }
}