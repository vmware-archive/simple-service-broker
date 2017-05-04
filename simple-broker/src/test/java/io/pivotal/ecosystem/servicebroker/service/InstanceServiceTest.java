/**
 Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.

 This program and the accompanying materials are made available under
 the terms of the under the Apache License, Version 2.0 (the "License”);
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package io.pivotal.ecosystem.servicebroker.service;

import io.pivotal.ecosystem.servicebroker.TestConfig;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class InstanceServiceTest {

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private ServiceInstance serviceInstance;

    @Autowired
    private CreateServiceInstanceRequest createServiceInstanceRequest;

    @Autowired
    private UpdateServiceInstanceRequest updateServiceInstanceRequest;

    @Autowired
    private DeleteServiceInstanceRequest deleteServiceInstanceRequest;

    @Autowired
    private HashOperations<String, Object, Object> hashOperations;

    @Autowired
    private RedisTemplate<String, ServiceInstance> instanceTemplate;

    private final Map<String, ServiceInstance> fakeRepo = new HashMap<>();

    @Test
    public void testInstance() throws ServiceBrokerException {
        given(instanceTemplate.opsForHash()).willReturn(hashOperations);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                fakeRepo.put(serviceInstance.getId(), serviceInstance);
                return null;
            }
        }).when(hashOperations).put(InstanceService.OBJECT_ID, serviceInstance.getId(), serviceInstance);

        assertNotNull(instanceService.createServiceInstance(createServiceInstanceRequest));

        when(instanceTemplate.opsForHash().get(Matchers.anyString(), Matchers.anyString())).thenReturn(fakeRepo.get(TestConfig.SI_ID));

        assertNotNull(instanceService.updateServiceInstance(updateServiceInstanceRequest));
        assertNotNull(instanceService.deleteServiceInstance(deleteServiceInstanceRequest));
    }

    @Test
    public void testUpdate() {

        instanceService.updateServiceInstance(updateServiceInstanceRequest);

    }
}