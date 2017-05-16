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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRedisRepositories
public class TestConfig {

    static final String SD_ID = "aUniqueId";
    static final String PLAN_ID = "anotherUniqueId";
    private static final String APP_GUID = "anAppGuid";
    private static final String ORG_GUID = "anOrgGuid";
    private static final String SPACE_GUID = "aSpaceGuid";

    @Autowired
    private Environment env;

    @Bean
    public String brokerUserId() {
        return env.getProperty("SECURITY_USER_NAME");
    }

    @Bean
    public String brokerPassword() {
        return env.getProperty("SECURITY_USER_PASSWORD");
    }

    @Bean
    public CatalogService catalogService() {
        return new CatalogService();
    }

    @Bean
    public RedisConnectionFactory connectionFactory() {
        return new JedisConnectionFactory();
    }

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    private static Map<String, Object> getBindResources() {
        Map<String, Object> m = new HashMap<>();
        m.put("app_guid", APP_GUID);
        return m;
    }

    static Map<String, Object> getParameters() {
        Map<String, Object> m = new HashMap<>();
        m.put("foo", "bar");
        m.put("bizz", "bazz");
        return m;
    }

    static CreateServiceInstanceBindingRequest createBindingRequest(String serviceInstanceId, String serviceBindingId) {
        CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest(SD_ID, PLAN_ID, APP_GUID, getBindResources(), getParameters());
        req.withBindingId(serviceBindingId);
        req.withServiceInstanceId(serviceInstanceId);
        return req;
    }

    static DeleteServiceInstanceBindingRequest deleteBindingRequest(String serviceInstanceId, String serviceBindingId) {
        return new DeleteServiceInstanceBindingRequest(serviceInstanceId, serviceBindingId, SD_ID, PLAN_ID, null);
    }

    @MockBean
    private DefaultServiceImpl mockDefaultServiceImpl;

    static CreateServiceInstanceRequest createRequest(String id, boolean async) {
        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(TestConfig.SD_ID, TestConfig.PLAN_ID, TestConfig.ORG_GUID, TestConfig.SPACE_GUID, TestConfig.getParameters());
        if (async) {
            req.withAsyncAccepted(true);
        }
        req.withServiceInstanceId(id);
        return req;
    }

    static UpdateServiceInstanceRequest updateRequest(String id, boolean async) {
        UpdateServiceInstanceRequest req = new UpdateServiceInstanceRequest(TestConfig.SD_ID, TestConfig.PLAN_ID);
        if (async) {
            req.withAsyncAccepted(true);
        }
        req.withServiceInstanceId(id);
        return req;
    }

    static DeleteServiceInstanceRequest deleteRequest(String id, boolean async) {
        return new DeleteServiceInstanceRequest(id, TestConfig.SD_ID, TestConfig.PLAN_ID, null, async);
    }

    static GetLastServiceOperationRequest lastOperationRequest(String id) {
        return new GetLastServiceOperationRequest(id);
    }

    static GetLastServiceOperationRequest getLastServiceOperationRequest(String id) {
        return new GetLastServiceOperationRequest(id);
    }
}