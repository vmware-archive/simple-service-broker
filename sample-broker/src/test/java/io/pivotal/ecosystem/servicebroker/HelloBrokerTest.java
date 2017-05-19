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

package io.pivotal.ecosystem.servicebroker;

import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.CatalogService;
import io.pivotal.ecosystem.servicebroker.service.ServiceInstanceRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloBrokerTest {

    private static final String ID = "deleteme";

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    private ServiceInstanceService serviceInstanceService;

    @Autowired
    private HelloBrokerRepository mockBrokerRepository;

    @Autowired
    private HelloBroker helloBroker;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        serviceInstanceRepository.deleteAll();
    }

    @Test
    public void testLifeCycle() {
        User user = new User("test", "pw", User.Role.Broker);
        when(mockBrokerRepository.provisionUser(any(User.class))).thenReturn(user);
        CreateServiceInstanceResponse csir = serviceInstanceService.createServiceInstance(TestConfig.createRequest(ID, false));
        assertNotNull(csir);

        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);

        GetLastServiceOperationResponse lo = serviceInstanceService.getLastOperation(TestConfig.lastOperationRequest(ID));
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());

        si.getParameters().put("password", "newPass");
        serviceInstanceRepository.save(si);

        user.setPassword("newPass");
        when(mockBrokerRepository.updateUser(user)).thenReturn(user);
        UpdateServiceInstanceResponse usir = serviceInstanceService.updateServiceInstance(TestConfig.updateRequest(ID, false));
        assertNotNull(usir);

        lo = serviceInstanceService.getLastOperation(TestConfig.lastOperationRequest(ID));
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());

        DeleteServiceInstanceResponse dsir = serviceInstanceService.deleteServiceInstance(TestConfig.deleteRequest(ID, false));
        assertNotNull(usir);

        lo = serviceInstanceService.getLastOperation(TestConfig.lastOperationRequest(ID));
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());

        si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
        assertTrue(si.isDeleted());
    }

}