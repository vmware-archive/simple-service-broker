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
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        if (serviceInstanceRepository.findOne(ID) != null) {
            serviceInstanceRepository.delete(ID);
        }
    }

    private InstanceService instanceServiceAsync() {
        when(mockDefaultServiceImpl.isAsync()).thenReturn(true);
        return new InstanceService(catalogService, mockDefaultServiceImpl, serviceInstanceRepository);
    }

    @Test
    public void testHappyLifeCycle() throws ServiceBrokerException {
        InstanceService service = instanceServiceAsync();
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));

        //grab an id to use throughout
        String id = UUID.randomUUID().toString();

        //nothing there to begin with
        assertNull(serviceInstanceRepository.findOne(id));

        //create an instance
        CreateServiceInstanceResponse csir = service.createServiceInstance(TestConfig.createRequest(id, true));
        assertNotNull(csir);

        ServiceInstance si = serviceInstanceRepository.findOne(id);
        assertNotNull(si);

        //state should be in progress
        GetLastServiceOperationResponse glosr = service.getLastOperation(TestConfig.lastOperationRequest(id));
        assertNotNull(glosr);
        assertEquals(OperationState.IN_PROGRESS, glosr.getState());
        assertFalse(glosr.isDeleteOperation());

        //force it to succeeded
        si.getLastOperation().setState(OperationState.SUCCEEDED);
        serviceInstanceRepository.save(si);
        si = serviceInstanceRepository.findOne(id);
        assertNotNull(si);

        glosr = service.getLastOperation(TestConfig.lastOperationRequest(id));
        assertNotNull(glosr);
        assertEquals(OperationState.SUCCEEDED, glosr.getState());
        assertFalse(glosr.isDeleteOperation());

        //update service
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.IN_PROGRESS, "updating."));
        UpdateServiceInstanceResponse usir = service.updateServiceInstance(TestConfig.updateRequest(id, true));
        assertNotNull(usir);

        //state should be in progress
        glosr = service.getLastOperation(TestConfig.lastOperationRequest(id));
        assertNotNull(glosr);
        assertEquals(OperationState.IN_PROGRESS, glosr.getState());
        assertFalse(glosr.isDeleteOperation());

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updating."));
        si = service.getServiceInstance(id);
        assertNotNull(si);

        //force it to succeeded
        si.getLastOperation().setState(OperationState.SUCCEEDED);
        serviceInstanceRepository.save(si);
        si = serviceInstanceRepository.findOne(id);
        assertNotNull(si);

        //delete service
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.IN_PROGRESS, "creating."));
        DeleteServiceInstanceResponse dsir = service.deleteServiceInstance(TestConfig.deleteRequest(id, true));
        assertNotNull(dsir);

        //state should be in progress
        glosr = service.getLastOperation(TestConfig.lastOperationRequest(id));
        assertNotNull(glosr);
        assertEquals(OperationState.IN_PROGRESS, glosr.getState());
        assertTrue(glosr.isDeleteOperation());

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "creating."));

        //it should be deleted once we ask for it again
        si = service.getServiceInstance(id);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        si = serviceInstanceRepository.findOne(id);
        assertNotNull(si);
        assertTrue(si.getDeleted());
    }

    @Test
    public void testSyncRequest() {
        InstanceService service = instanceServiceAsync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        service.createServiceInstance(TestConfig.createRequest(ID, false));

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        service.updateServiceInstance(TestConfig.updateRequest(ID, false));

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, false));
    }

    @Test
    public void testDuplicateCreate() {
        InstanceService service = instanceServiceAsync();
        service.createServiceInstance(TestConfig.createRequest(ID, true));

        exception.expect(ServiceInstanceExistsException.class);
        service.createServiceInstance(TestConfig.createRequest(ID, true));

        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        si.getLastOperation().setState(OperationState.SUCCEEDED);
        serviceInstanceRepository.save(si);

        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testUpdateWhileInProgress() {
        InstanceService service = instanceServiceAsync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "updating."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.IN_PROGRESS, "updating."));
        exception.expect(ServiceBrokerException.class);
        service.updateServiceInstance(TestConfig.updateRequest(ID, true));
    }

    @Test
    public void testDeleteWhileInProgress() {
        InstanceService service = instanceServiceAsync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "deleting."));
        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "deleting."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.IN_PROGRESS, "deleting."));
        exception.expect(ServiceBrokerException.class);
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testFailedCreate() {
        InstanceService service = instanceServiceAsync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "created."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.FAILED, "not created."));
        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());

        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertTrue(si.getDeleted());
    }

    @Test
    public void testFailedUpdate() {
        InstanceService service = instanceServiceAsync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "not updated."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.FAILED, "not updated."));
        service.updateServiceInstance(TestConfig.updateRequest(ID, true));

        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());

        //a failed update should leave the instance back where it started.
        assertNotNull(serviceInstanceRepository.findOne(ID));
    }

    @Test
    public void testFailedDelete() {
        InstanceService service = instanceServiceAsync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.FAILED, "not deleted."));
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));

        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());

        //if delete failed, should not be marked as deleted
        assertFalse(si.getDeleted());
    }

    @Test
    public void testLastOperationCRUD() {
        InstanceService service = instanceServiceAsync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        service.createServiceInstance(TestConfig.createRequest(ID, true));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        LastOperation lo = si.getLastOperation();
        assertNotNull(lo);
        assertEquals(OperationState.IN_PROGRESS, lo.getState());
        assertEquals(Operation.CREATE, lo.getOperation());

        GetLastServiceOperationRequest glsoreq = new GetLastServiceOperationRequest(ID);
        GetLastServiceOperationResponse glsoresp = service.getLastOperation(glsoreq);
        assertNotNull(glsoresp);
        assertEquals(OperationState.IN_PROGRESS, glsoresp.getState());
        assertFalse(glsoresp.isDeleteOperation());

        si.getLastOperation().setState(OperationState.SUCCEEDED);
        si.getLastOperation().setOperation(Operation.DELETE);
        serviceInstanceRepository.save(si);

        si = serviceInstanceRepository.findOne(ID);
        lo = si.getLastOperation();
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());
        assertEquals(Operation.DELETE, lo.getOperation());

        glsoresp = service.getLastOperation(glsoreq);
        assertNotNull(glsoresp);
        assertEquals(OperationState.SUCCEEDED, glsoresp.getState());
        assertTrue(glsoresp.isDeleteOperation());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }

    @Test
    public void testDeletedAndNonExistent() {
        InstanceService service = instanceServiceAsync();

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        assertNotNull(service.createServiceInstance(TestConfig.createRequest(ID, true)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        si.setDeleted(true);
        serviceInstanceRepository.save(si);

        exception.expect(ServiceInstanceExistsException.class);
        service.createServiceInstance(TestConfig.createRequest(ID, true));

        exception.expect(ServiceInstanceExistsException.class);
        service.updateServiceInstance(TestConfig.updateRequest(ID, true));

        exception.expect(ServiceInstanceExistsException.class);
        service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
    }
}