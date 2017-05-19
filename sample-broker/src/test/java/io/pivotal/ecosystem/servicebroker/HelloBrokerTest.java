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

import io.pivotal.ecosystem.servicebroker.model.LastOperation;
import io.pivotal.ecosystem.servicebroker.model.Operation;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.CatalogService;
import io.pivotal.ecosystem.servicebroker.service.DefaultServiceImpl;
import io.pivotal.ecosystem.servicebroker.service.InstanceService;
import io.pivotal.ecosystem.servicebroker.service.ServiceInstanceRepository;
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
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloBrokerTest {

    private static final String ID = "deleteme";

    @Autowired
    private CatalogService catalogService;

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
        if (serviceInstanceRepository.findOne(ID) != null) {
            serviceInstanceRepository.delete(ID);
        }
    }

    @Test
    public void testHappyLifeCycle() {
        when(mockBrokerRepository.provisionUser(any(User.class))).thenReturn(new User("hello", "pw", "broker"));
        CreateServiceInstanceResponse csir = serviceInstanceService.createServiceInstance(TestConfig.createRequest(ID, false));


        ServiceInstance si = serviceInstanceRepository.findOne(ID);
        assertNotNull(si);
//
//        //create an instance
//        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "creating."));
//        CreateServiceInstanceResponse csir = service.createServiceInstance(TestConfig.createRequest(ID, true));
//        assertNotNull(csir);
//
//        ServiceInstance si = serviceInstanceRepository.findOne(ID);
//        assertNotNull(si);
//        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
//        assertFalse(si.isDeleted());
//
//        service.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
//        si = serviceInstanceRepository.findOne(ID);
//        assertNotNull(si);
//        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
//        assertFalse(si.isDeleted());
//
//        //update service
//        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updating."));
//        UpdateServiceInstanceResponse usir = service.updateServiceInstance(TestConfig.updateRequest(ID, true));
//        assertNotNull(usir);
//
//        service.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
//        si = serviceInstanceRepository.findOne(ID);
//        assertNotNull(si);
//        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
//        assertFalse(si.isDeleted());
//
//        //delete service
//        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleting."));
//        DeleteServiceInstanceResponse dsir = service.deleteServiceInstance(TestConfig.deleteRequest(ID, true));
//        assertNotNull(dsir);
//
//        service.getLastOperation(TestConfig.getLastServiceOperationRequest(ID));
//        si = serviceInstanceRepository.findOne(ID);
//        assertNotNull(si);
//        assertEquals(OperationState.SUCCEEDED, si.getLastOperation().getState());
//        assertTrue(si.isDeleted());
    }

}