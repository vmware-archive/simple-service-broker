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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.*;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;
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
    public void testThatInProgressCreateFails() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        service.createServiceInstance(TestConfig.createRequest(ID, true));
    }

    @Test
    public void testThatInProgressUpdateFails() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "creating."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.IN_PROGRESS, "updating."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        service.updateServiceInstance(TestConfig.updateRequest(ID, true));
    }

    @Test
    public void testThatInProgressDeleteFails() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "creating."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.IN_PROGRESS, "deleteing."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testThatBogusUpdateIdsAreRejected() {
        InstanceService service = instanceServiceSync();

        exception.expect(ServiceInstanceDoesNotExistException.class);
        service.updateServiceInstance(TestConfig.updateRequest("bogus", true));
    }

    @Test
    public void testThatBogusDeleteIdsAreRejected() {
        InstanceService service = instanceServiceSync();

        exception.expect(ServiceInstanceDoesNotExistException.class);
        service.updateServiceInstance(TestConfig.updateRequest("bogus", true));
    }

    @Test
    public void testThatDeletedIdUpdatesAreRejected() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        service.createServiceInstance(TestConfig.createRequest(ID, true));
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));

        exception.expect(ServiceInstanceDoesNotExistException.class);
        service.updateServiceInstance(TestConfig.updateRequest(ID, true));
    }

    @Test
    public void testThatDeletedIdDeletesAreRejected() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        service.createServiceInstance(TestConfig.createRequest(ID, true));
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));

        exception.expect(ServiceInstanceDoesNotExistException.class);
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testThatDuplicateIdCreatesAreRejected() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        service.createServiceInstance(TestConfig.createRequest(ID, true));

        exception.expect(ServiceInstanceExistsException.class);
        service.createServiceInstance(TestConfig.createRequest(ID, true));
    }

    @Test
    public void testFailedRequests() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.FAILED, null));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());
        assertTrue(si.isDeleted());

        serviceInstanceRepository.delete(ID);

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, null));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.FAILED, null));
        assertNotNull(service.updateServiceInstance(TestConfig.updateRequest(ID, true)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.FAILED, null));
        assertNotNull(service.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());
    }

    @Test
    public void testBrokerCreateException() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenThrow(new ServiceBrokerException("aaaagh!"));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());
        assertTrue(si.isDeleted());
    }

    @Test
    public void testBrokerOtherExceptions() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, null));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, false)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenThrow(new ServiceBrokerException("noooooo!"));
        assertNotNull(service.updateServiceInstance(TestConfig.updateRequest(ID, false)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenThrow(new ServiceBrokerException("ooof!"));
        assertNotNull(service.deleteServiceInstance(TestConfig.deleteRequest(ID, false)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());
    }

    @Test
    public void testLastOperation() {
        InstanceService service = instanceServiceSync();

        //successful create
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        //failed create
        GetLastServiceOperationResponse glsoresp = service.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        assertNotNull(glsoresp);
        assertEquals(OperationState.SUCCEEDED, glsoresp.getState());

        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        serviceInstanceRepository.delete(ID);

        //failed delete
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.FAILED, "deleted."));
        assertNotNull(service.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        assertNotNull(service.getLastOperation(TestConfig.getLastServiceOperationRequest(ID)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        //successful delete
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.FAILED, "deleted."));
        assertNotNull(service.deleteServiceInstance(TestConfig.deleteRequest(ID, true)));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertFalse(si.isDeleted());

        //bogus id
        exception.expect(ServiceInstanceDoesNotExistException.class);
        service.getLastOperation(TestConfig.getLastServiceOperationRequest("bogus"));
    }

    @Test
    public void testHappyLifeCycle() throws ServiceBrokerException {
        InstanceService service = instanceServiceSync();

        //create an instance
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "creating."));
        CreateServiceInstanceResponse csir = service.createServiceInstance(TestConfig.createRequest(ID, true));
        assertNotNull(csir);

        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        service.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        //update service
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updating."));
        UpdateServiceInstanceResponse usir = service.updateServiceInstance(TestConfig.updateRequest(ID, true));
        assertNotNull(usir);

        service.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        assertFalse(si.isDeleted());

        //delete service
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleting."));
        DeleteServiceInstanceResponse dsir = service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
        assertNotNull(dsir);

        service.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        assertTrue(si.isDeleted());
    }

    @Test
    public void testAsyncCreateResponse() {
        InstanceService service = instanceServiceSync();
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "created."));
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "created."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        service.createServiceInstance(TestConfig.createRequest(ID, true));
    }

    @Test
    public void testOtherAsyncResponses() {
        InstanceService service = instanceServiceSync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, false)));

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.IN_PROGRESS, "updated."));
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.IN_PROGRESS, "deleted."));
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "created."));

        exception.expect(ServiceBrokerInvalidParametersException.class);
        service.updateServiceInstance(TestConfig.updateRequest(ID, true));
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }
}