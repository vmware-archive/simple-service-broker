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

package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class BindingServiceTest {

    @Autowired
    private BindingService bindingService;

    @Autowired
    private ServiceInstance serviceInstance;

    @Autowired
    private ServiceBinding serviceBinding;

    @Autowired
    private CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest;

    @Autowired
    private DeleteServiceInstanceBindingRequest deleteBindingRequest;

    private final Map<String, ServiceBinding> fakeRepo = new HashMap<>();

    @Autowired
    private RedisTemplate<String, ServiceBinding> bindingTemplate;

    @Autowired
    private HashOperations<String, Object, Object> hashOperations;

    @MockBean
    private InstanceService instanceService;

    @Test
    public void testBinding() throws ServiceBrokerException {
        given(bindingTemplate.opsForHash()).willReturn(hashOperations);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                fakeRepo.put(serviceBinding.getId(), serviceBinding);
                return null;
            }
        }).when(hashOperations).put(anyString(), anyString(), any(ServiceBinding.class));

        given(instanceService.getServiceInstance(TestConfig.SI_ID))
                .willReturn(serviceInstance);

        CreateServiceInstanceAppBindingResponse cresp = (CreateServiceInstanceAppBindingResponse)
                bindingService.createServiceInstanceBinding(createServiceInstanceBindingRequest);
        assertNotNull(cresp);
        assertNotNull(cresp.getCredentials());

        when(hashOperations.get(Matchers.anyString(), Matchers.anyString())).thenReturn(fakeRepo.get(TestConfig.SB_ID));

        bindingService.deleteServiceInstanceBinding(deleteBindingRequest);
    }
}