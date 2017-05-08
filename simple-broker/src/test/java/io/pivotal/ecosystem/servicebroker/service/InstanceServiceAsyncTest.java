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
import org.springframework.cloud.servicebroker.exception.ServiceBrokerAsyncRequiredException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceServiceAsyncTest {

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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private InstanceService instanceServiceAsync() {
        return new InstanceService(catalogService, new DefaultServiceAsync(), serviceInstanceRepository);
    }

    @Test
    public void testInstance() throws ServiceBrokerException {
        InstanceService service = instanceServiceAsync();
        String id = createServiceInstanceRequestAsync.getServiceInstanceId();

        assertNull(serviceInstanceRepository.findOne(id));

        CreateServiceInstanceResponse csir = service.createServiceInstance(createServiceInstanceRequestAsync);
        assertNotNull(csir);

        assertNotNull(serviceInstanceRepository.findOne(id));

        GetLastServiceOperationResponse glosr = service.getLastOperation(getLastServiceOperationRequest);
        assertNotNull(glosr);
        assertEquals(OperationState.IN_PROGRESS, glosr.getState());

        ServiceInstance si = service.getServiceInstance(id);
        assertNotNull(si);

        UpdateServiceInstanceResponse usir = service.updateServiceInstance(updateServiceInstanceRequestAsync.withServiceInstanceId(id));
        assertNotNull(usir);

        DeleteServiceInstanceResponse dsir = service.deleteServiceInstance(deleteServiceInstanceRequestAsync);
        assertNotNull(dsir);

        assertNull(serviceInstanceRepository.findOne(id));
    }

    @Test
    public void testSyncService() {
        InstanceService service = instanceServiceAsync();

        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.createServiceInstance(createServiceInstanceRequest);

        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.updateServiceInstance(updateServiceInstanceRequest);

        exception.expect(ServiceBrokerAsyncRequiredException.class);
        service.deleteServiceInstance(deleteServiceInstanceRequest);
    }

//    @Test
//    public void testUpdateWhileInProgress() {
//        fail();
//    }
//
//    @Test
//    public void testDeleteWhileInProgress() {
//        fail();
//    }

}