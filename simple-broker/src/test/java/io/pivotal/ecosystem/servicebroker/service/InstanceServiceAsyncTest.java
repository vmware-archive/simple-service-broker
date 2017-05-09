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
    private CreateServiceInstanceRequest createServiceInstanceRequestAsync;

    @Autowired
    private CreateServiceInstanceRequest createServiceInstanceRequest;

    @Autowired
    private UpdateServiceInstanceRequest updateServiceInstanceRequestAsync;

    @Autowired
    private UpdateServiceInstanceRequest updateServiceInstanceRequest;

    @Autowired
    private DeleteServiceInstanceRequest deleteServiceInstanceRequestAsync;

    @Autowired
    private DeleteServiceInstanceRequest deleteServiceInstanceRequest;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    private GetLastServiceOperationRequest getLastServiceOperationRequest;

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
        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(OperationState.SUCCEEDED);

        //grab an id to use throughout
        String id = createServiceInstanceRequestAsync.getServiceInstanceId();

        //nothing there to begin with
        assertNull(serviceInstanceRepository.findOne(id));

        //create an instance
        CreateServiceInstanceResponse csir = service.createServiceInstance(createServiceInstanceRequestAsync);
        assertNotNull(csir);

        ServiceInstance si = serviceInstanceRepository.findOne(id);
        assertNotNull(si);

        //state should be in progress
        GetLastServiceOperationResponse glosr = service.getLastOperation(getLastServiceOperationRequest);
        assertNotNull(glosr);
        assertEquals(OperationState.IN_PROGRESS, glosr.getState());
        assertFalse(glosr.isDeleteOperation());

        //force it to succeeded
        si.getLastOperation().setState(OperationState.SUCCEEDED);
        serviceInstanceRepository.save(si);
        si = serviceInstanceRepository.findOne(id);
        assertNotNull(si);

        glosr = service.getLastOperation(getLastServiceOperationRequest);
        assertNotNull(glosr);
        assertEquals(OperationState.SUCCEEDED, glosr.getState());
        assertFalse(glosr.isDeleteOperation());

        //update service
        UpdateServiceInstanceResponse usir = service.updateServiceInstance(updateServiceInstanceRequestAsync.withServiceInstanceId(id));
        assertNotNull(usir);

        //state should be in progress
        glosr = service.getLastOperation(getLastServiceOperationRequest);
        assertNotNull(glosr);
        assertEquals(OperationState.IN_PROGRESS, glosr.getState());
        assertFalse(glosr.isDeleteOperation());

        si = service.getServiceInstance(id);
        assertNotNull(si);

        //force it to succeeded
        si.getLastOperation().setState(OperationState.SUCCEEDED);
        serviceInstanceRepository.save(si);
        si = serviceInstanceRepository.findOne(id);
        assertNotNull(si);

        //delete service
        DeleteServiceInstanceResponse dsir = service.deleteServiceInstance(deleteServiceInstanceRequestAsync);
        assertNotNull(dsir);

        //state should be in progress
        glosr = service.getLastOperation(getLastServiceOperationRequest);
        assertNotNull(glosr);
        assertEquals(OperationState.IN_PROGRESS, glosr.getState());
        assertTrue(glosr.isDeleteOperation());

        //it should be deleted once we ask for it again
        si = service.getServiceInstance(id);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
        assertNull(serviceInstanceRepository.findOne(id));
    }

    @Test
    public void testAsynchServiceWithSyncRequest() {
        InstanceService service = instanceServiceAsync();

        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.createServiceInstance(createServiceInstanceRequest);

        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.updateServiceInstance(updateServiceInstanceRequest);

        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.deleteServiceInstance(deleteServiceInstanceRequest);
    }

    @Test
    public void testDuplicateCreate() {
        InstanceService service = instanceServiceAsync();

        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(TestConfig.SD_ID, TestConfig.PLAN_ID, TestConfig.ORG_GUID, TestConfig.SPACE_GUID, TestConfig.getParameters());
        req.withServiceInstanceId(ID);
        req.withAsyncAccepted(true);

        service.createServiceInstance(req);

        exception.expect(ServiceInstanceExistsException.class);
        service.createServiceInstance(req);

        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        si.getLastOperation().setState(OperationState.SUCCEEDED);
        serviceInstanceRepository.save(si);

        DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(ID, TestConfig.SD_ID, TestConfig.PLAN_ID,
                catalogService.getServiceDefinition(TestConfig.SD_ID));

        service.deleteServiceInstance(dreq);
    }

    @Test
    public void testUpdateWhileInProgress() {
        InstanceService service = instanceServiceAsync();

        assertNotNull(service.createServiceInstance(createRequest(ID)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(OperationState.IN_PROGRESS);
        exception.expect(ServiceBrokerException.class);
        service.updateServiceInstance(updateRequest(ID));
    }

    @Test
    public void testDeleteWhileInProgress() {
        InstanceService service = instanceServiceAsync();

        assertNotNull(service.createServiceInstance(createRequest(ID)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(OperationState.IN_PROGRESS);
        exception.expect(ServiceBrokerException.class);
        service.deleteServiceInstance(deleteRequest(ID));
    }

    @Test
    public void testFailedCreate() {
        InstanceService service = instanceServiceAsync();

        assertNotNull(service.createServiceInstance(createRequest(ID)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(si)).thenReturn(OperationState.FAILED);
        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());

        assertNull(serviceInstanceRepository.findOne(ID));
    }

    @Test
    public void testFailedUpdate() {
        InstanceService service = instanceServiceAsync();

        assertNotNull(service.createServiceInstance(createRequest(ID)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(si)).thenReturn(OperationState.SUCCEEDED);
        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(OperationState.FAILED);
        service.updateServiceInstance(updateRequest(ID));

        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());

        //a failed update should leave the instance back where it started.
        assertNotNull(serviceInstanceRepository.findOne(ID));
    }

    @Test
    public void testFailedDelete() {
        InstanceService service = instanceServiceAsync();

        assertNotNull(service.createServiceInstance(createRequest(ID)));
        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        when(mockDefaultServiceImpl.getServiceStatus(si)).thenReturn(OperationState.SUCCEEDED);
        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());

        service.deleteServiceInstance(deleteRequest(ID));

        when(mockDefaultServiceImpl.getServiceStatus(any(ServiceInstance.class))).thenReturn(OperationState.FAILED);
        si = service.getServiceInstance(ID);
        assertNotNull(si);
        assertEquals(OperationState.FAILED, si.getLastOperation().getState());

        assertNull(serviceInstanceRepository.findOne(ID));
    }

    @Test
    public void testLastOperationCRUD() {
        InstanceService service = instanceServiceAsync();

        service.createServiceInstance(createRequest(ID));
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

        DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(ID, TestConfig.SD_ID, TestConfig.PLAN_ID,
                catalogService.getServiceDefinition(TestConfig.SD_ID));

        service.deleteServiceInstance(dreq);
    }

    private CreateServiceInstanceRequest createRequest(String id) {
        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(TestConfig.SD_ID, TestConfig.PLAN_ID, TestConfig.ORG_GUID, TestConfig.SPACE_GUID, TestConfig.getParameters());
        req.withAsyncAccepted(true);
        req.withServiceInstanceId(id);
        return req;
    }

    private UpdateServiceInstanceRequest updateRequest(String id) {
        UpdateServiceInstanceRequest req = new UpdateServiceInstanceRequest(TestConfig.SD_ID, TestConfig.PLAN_ID);
        req.withAsyncAccepted(true);
        req.withServiceInstanceId(id);
        return req;
    }

    private DeleteServiceInstanceRequest deleteRequest(String id) {
        return new DeleteServiceInstanceRequest(id, TestConfig.SD_ID, TestConfig.PLAN_ID, catalogService.getServiceDefinition(TestConfig.SD_ID), true);
    }
}