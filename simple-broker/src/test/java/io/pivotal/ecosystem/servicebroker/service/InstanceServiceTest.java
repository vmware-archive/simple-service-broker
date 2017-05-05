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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceServiceTest {

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private CreateServiceInstanceRequest createServiceInstanceRequest;

    @Autowired
    private UpdateServiceInstanceRequest updateServiceInstanceRequest;

    @Autowired
    private DeleteServiceInstanceRequest deleteServiceInstanceRequest;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Test
    public void testInstance() throws ServiceBrokerException {
        assertNull(serviceInstanceRepository.findOne(createServiceInstanceRequest.getServiceInstanceId()));

        CreateServiceInstanceResponse csir = instanceService.createServiceInstance(createServiceInstanceRequest);
        assertNotNull(csir);
        assertNotNull(serviceInstanceRepository.findOne(createServiceInstanceRequest.getServiceInstanceId()));

        ServiceInstance si = instanceService.getServiceInstance(createServiceInstanceRequest.getServiceInstanceId());
        assertNotNull(si);

        UpdateServiceInstanceResponse usir = instanceService.updateServiceInstance(updateServiceInstanceRequest);
        assertNotNull(usir);

        DeleteServiceInstanceResponse dsir = instanceService.deleteServiceInstance(deleteServiceInstanceRequest);
        assertNotNull(dsir);

        assertNull(serviceInstanceRepository.findOne(createServiceInstanceRequest.getServiceInstanceId()));
    }

}