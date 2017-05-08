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

import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceServiceTest {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CreateServiceInstanceRequest createServiceInstanceRequest;

    @Autowired
    private UpdateServiceInstanceRequest updateServiceInstanceRequest;

    @Autowired
    private DeleteServiceInstanceRequest deleteServiceInstanceRequest;

    @Autowired
    private CreateServiceInstanceRequest createServiceInstanceRequestAsync;

    @Autowired
    private UpdateServiceInstanceRequest updateServiceInstanceRequestAsync;

    @Autowired
    private DeleteServiceInstanceRequest deleteServiceInstanceRequestAsync;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    private GetLastServiceOperationRequest getLastServiceOperationRequest;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private InstanceService instanceServiceSync() {
        return new InstanceService(catalogService, new DefaultServiceImpl(), serviceInstanceRepository);
    }

    @Test
    public void testInstance() throws ServiceBrokerException {
        InstanceService service = instanceServiceSync();

        assertNull(serviceInstanceRepository.findOne(createServiceInstanceRequest.getServiceInstanceId()));

        CreateServiceInstanceResponse csir = service.createServiceInstance(createServiceInstanceRequest);
        assertNotNull(csir);
        assertNotNull(serviceInstanceRepository.findOne(createServiceInstanceRequest.getServiceInstanceId()));

        ServiceInstance si = service.getServiceInstance(createServiceInstanceRequest.getServiceInstanceId());
        assertNotNull(si);

        GetLastServiceOperationResponse glsor = service.getLastOperation(getLastServiceOperationRequest);
        assertNotNull(glsor);
        assertEquals(OperationState.SUCCEEDED, glsor.getState());

        UpdateServiceInstanceResponse usir = service.updateServiceInstance(updateServiceInstanceRequest);
        assertNotNull(usir);

        si = service.getServiceInstance(createServiceInstanceRequest.getServiceInstanceId());
        assertNotNull(si);

        glsor = service.getLastOperation(getLastServiceOperationRequest);
        assertNotNull(glsor);
        assertEquals(OperationState.SUCCEEDED, glsor.getState());

        DeleteServiceInstanceResponse dsir = service.deleteServiceInstance(deleteServiceInstanceRequest);
        assertNotNull(dsir);

        assertNull(serviceInstanceRepository.findOne(createServiceInstanceRequest.getServiceInstanceId()));
    }

    @Test
    public void testAsyncRequest() {
        InstanceService service = instanceServiceSync();
        service.createServiceInstance(createServiceInstanceRequestAsync);
        service.updateServiceInstance(updateServiceInstanceRequestAsync);
        service.deleteServiceInstance(deleteServiceInstanceRequestAsync);
    }

    @Test
    public void testDuplicateCreate() {
        InstanceService service = instanceServiceSync();
        String id = "deleteme";

        if(serviceInstanceRepository.findOne(id) != null) {
            serviceInstanceRepository.delete(id);
        }

        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(TestConfig.SD_ID, TestConfig.PLAN_ID, TestConfig.ORG_GUID, TestConfig.SPACE_GUID, TestConfig.getParameters());
        req.withServiceInstanceId(id);

        service.createServiceInstance(req);

        exception.expect(ServiceInstanceExistsException.class);
        service.createServiceInstance(req);

        DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(id, TestConfig.SD_ID, TestConfig.PLAN_ID,
                catalogService.getServiceDefinition(TestConfig.SD_ID));

        service.deleteServiceInstance(dreq);
    }
}