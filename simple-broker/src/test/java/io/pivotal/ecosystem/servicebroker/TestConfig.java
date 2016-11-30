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

package io.pivotal.ecosystem.servicebroker;

import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TestConfig {

    public static final String SI_ID = "siId";
    public static final String SB_ID = "sbId";
    public static final String SD_ID = "aUniqueId";

    private static final String PLAN_ID = "anotherUniqueId";
    private static final String APP_GUID = "anAppGuid";
    private static final String ORG_GUID = "anOrgGuid";
    private static final String SPACE_GUID = "aSpaceGuid";

    @Bean
    public CatalogService catalogService() {
        return new CatalogService();
    }

    @Bean
    public InstanceService instanceService(CatalogService catalogService, BrokeredService brokeredService,
                                           RedisTemplate<String, ServiceInstance> instanceTemplate) {
        return new InstanceService(catalogService, brokeredService, instanceTemplate);
    }

    @Bean
    public BindingService bindingService(InstanceService instanceService, BrokeredService brokeredService,
                                         RedisTemplate<String, ServiceBinding> bindingTemplate) {
        return new BindingService(instanceService, brokeredService, bindingTemplate);
    }

    @Bean
    public BrokeredService brokeredService() {
        return new DefaultServiceImpl();
    }

    @MockBean
    private RedisTemplate<String, ServiceInstance> instanceTemplate;

    @MockBean
    private RedisTemplate<String, ServiceBinding> bindingTemplate;

    @MockBean
    private HashOperations<String, Object, Object> hashOperations;

    @Bean
    public CreateServiceInstanceRequest createServiceInstanceRequest() {
        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(SD_ID, PLAN_ID, ORG_GUID, SPACE_GUID, getParameters());
        req.withServiceInstanceId(SI_ID);
        return req;
    }

    @Bean
    public ServiceInstance serviceInstance(CreateServiceInstanceRequest req) {
        return new ServiceInstance(req);
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
    public CreateServiceInstanceBindingRequest createBindingRequest() {
        CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest(SD_ID, PLAN_ID, APP_GUID,
                getBindResources(), getParameters());
        req.withBindingId(SB_ID);
        req.withServiceInstanceId(SI_ID);
        return req;
    }

    @Bean
    public ServiceBinding serviceBinding(CreateServiceInstanceBindingRequest req) {
        return new ServiceBinding(req);
    }

    @Bean
    public DeleteServiceInstanceBindingRequest deleteBindingRequest() {
        return new DeleteServiceInstanceBindingRequest(SI_ID, SB_ID, SD_ID, PLAN_ID,
                catalogService().getServiceDefinition(SD_ID));
    }

    @Bean
    UpdateServiceInstanceRequest updateServiceInstanceRequest() {
        UpdateServiceInstanceRequest req = new UpdateServiceInstanceRequest(SD_ID, PLAN_ID, getParameters());
        req.withServiceDefinition(catalogService().getServiceDefinition(SD_ID));
        req.withServiceInstanceId(SI_ID);
        return req;
    }

    @Bean
    DeleteServiceInstanceRequest deleteServiceInstanceRequest() {
        DeleteServiceInstanceRequest req = new DeleteServiceInstanceRequest(SI_ID, SD_ID, PLAN_ID,
                catalogService().getServiceDefinition(SD_ID));
        return req;
    }
}