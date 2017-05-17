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
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.BrokeredService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloBrokerRepositoryTest {

    @Autowired
    private HelloBrokerRepository mockRepo;

    @Autowired
    private BrokeredService helloBroker;

    @Autowired
    private ServiceInstance serviceInstance;

    @Autowired
    private ServiceBinding serviceBinding;

    @Autowired
    private User instanceUser;

    @Autowired
    private User bindingUser;

    @Before
    public void setUp() {
        serviceInstance.getParameters().remove("user");
    }

    @Test
    public void testProvision() throws ServiceBrokerException {
        given(this.mockRepo.provisionUser(instanceUser))
                .willReturn(new User(instanceUser.getName(), TestConfig.PASSWORD, instanceUser.getRole()));

        LastOperation lo = helloBroker.createInstance(serviceInstance);
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());

        User user = (User) serviceInstance.getParameters().get("user");
        assertNotNull(user);
        assertEquals(TestConfig.SI_ID, user.getName());
        assertEquals(User.Role.Broker, user.getRole());
        assertEquals(TestConfig.PASSWORD, user.getPassword());
    }

    @Test
    public void testDeprovision() throws ServiceBrokerException {
        serviceInstance.getParameters().put("user", instanceUser);

        LastOperation lo = helloBroker.deleteInstance(serviceInstance);
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());
        assertFalse(serviceInstance.getParameters().containsKey("user"));
    }

    @Test
    public void testBinding() {
        given(this.mockRepo.provisionUser(bindingUser))
                .willReturn(new User(bindingUser.getName(), TestConfig.PASSWORD, bindingUser.getRole()));

        LastOperation lo = helloBroker.createBinding(serviceInstance, serviceBinding);
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());

        User user = (User) serviceBinding.getParameters().get("user");
        assertNotNull(user);
        assertEquals(TestConfig.SB_ID, user.getName());
        assertEquals(User.Role.User, user.getRole());
        assertEquals(TestConfig.PASSWORD, user.getPassword());
    }

    @Test
    public void testDeleteBinding() {
        serviceBinding.getParameters().put("user", bindingUser);

        LastOperation lo = helloBroker.deleteBinding(serviceInstance, serviceBinding);
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());
        assertFalse(serviceBinding.getParameters().containsKey("user"));
    }

    @Test
    public void testCredentials() {
        bindingUser.setPassword(TestConfig.PASSWORD);
        serviceBinding.getParameters().put("user", bindingUser);

        Map<String, Object> m = helloBroker.getCredentials(serviceInstance, serviceBinding);
        assertNotNull(m);
        assertEquals("localhost", m.get("hostname"));
        assertEquals("8080", m.get("port"));
        assertEquals(TestConfig.SB_ID, m.get("username"));
        assertEquals(TestConfig.PASSWORD, m.get("password"));
        assertEquals("hello://sbId:password@localhost:8080", m.get("uri"));
    }

    @Test
    public void testAsync() {
        assertFalse(helloBroker.isAsync());
    }

    @Test
    public void testInstanceUpdate() {
        given(this.mockRepo.updateUser(instanceUser.getName(), instanceUser))
                .willReturn(new User(instanceUser.getName(), "newPassword", instanceUser.getRole()));

        serviceInstance.getParameters().put("user", instanceUser);

        LastOperation lo = helloBroker.updateInstance(serviceInstance);
        assertNotNull(lo);
        assertEquals(OperationState.SUCCEEDED, lo.getState());

        User user = (User) serviceInstance.getParameters().get("user");
        assertNotNull(user);
        assertEquals(TestConfig.SI_ID, user.getName());
        assertEquals(User.Role.Broker, user.getRole());
        assertEquals("newPassword", user.getPassword());
    }
}