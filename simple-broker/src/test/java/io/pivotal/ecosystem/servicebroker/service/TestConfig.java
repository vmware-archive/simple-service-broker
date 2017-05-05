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

import org.springframework.cloud.servicebroker.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRedisRepositories
public class TestConfig {

    static final String SD_ID = "aUniqueId";
    private static final String PLAN_ID = "anotherUniqueId";
    private static final String APP_GUID = "anAppGuid";
    private static final String ORG_GUID = "anOrgGuid";
    private static final String SPACE_GUID = "aSpaceGuid";

    @Bean
    public CatalogService catalogService() {
        return new CatalogService();
    }

    @Bean
    public RedisConnectionFactory connectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public BrokeredService brokeredService() {
        return new DefaultServiceImpl();
    }

    @Bean
    public CreateServiceInstanceRequest createServiceInstanceRequest(String serviceInstanceId) {
        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(SD_ID, PLAN_ID, ORG_GUID, SPACE_GUID, getParameters());
        req.withServiceInstanceId(serviceInstanceId);
        return req;
    }

    @Bean
    public String serviceBindingId() {
        return "sbd" + System.currentTimeMillis();
    }

    @Bean
    public String serviceInstanceId() {
        return "sid" + System.currentTimeMillis();
    }

    private Map<String, Object> getBindResources() {
        Map<String, Object> m = new HashMap<>();
        m.put("app_guid", APP_GUID);
        return m;
    }

    private Map<String, Object> getParameters() {
        Map<String, Object> m = new HashMap<>();
        m.put("foo", "bar");
        m.put("bizz", "bazz");
        return m;
    }

    @Bean
    public CreateServiceInstanceBindingRequest createBindingRequest(String serviceInstanceId, String serviceBindingId) {
        CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest(SD_ID, PLAN_ID, APP_GUID,
                getBindResources(), getParameters());
        req.withBindingId(serviceBindingId);
        req.withServiceInstanceId(serviceInstanceId);
        return req;
    }

    @Bean
    public DeleteServiceInstanceBindingRequest deleteBindingRequest(String serviceInstanceId, String serviceBindingId) {
        return new DeleteServiceInstanceBindingRequest(serviceInstanceId, serviceBindingId, SD_ID, PLAN_ID,
                catalogService().getServiceDefinition(SD_ID));
    }

    @Bean
    UpdateServiceInstanceRequest updateServiceInstanceRequest(String serviceInstanceId) {
        UpdateServiceInstanceRequest req = new UpdateServiceInstanceRequest(SD_ID, PLAN_ID, getParameters());
        req.withServiceDefinition(catalogService().getServiceDefinition(SD_ID));
        req.withServiceInstanceId(serviceInstanceId);
        return req;
    }

    @Bean
    DeleteServiceInstanceRequest deleteServiceInstanceRequest(String serviceInstanceId) {
        DeleteServiceInstanceRequest req = new DeleteServiceInstanceRequest(serviceInstanceId, SD_ID, PLAN_ID,
                catalogService().getServiceDefinition(SD_ID));
        return req;
    }
}