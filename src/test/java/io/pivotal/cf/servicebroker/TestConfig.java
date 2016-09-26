package io.pivotal.cf.servicebroker;

import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TestConfig {

    public static final String SI_ID = "siId";
    public static final String SB_ID = "sbId";
    public static final String SD_ID = "aUniqueId";
    public static final String PLAN_ID = "anotherUniqueId";
    public static final String APP_GUID = "anAppGuid";
    public static final String ORG_GUID = "anOrgGuid";
    public static final String SPACE_GUID = "aSpaceGuid";

    @Autowired
    private CatalogService catalogService;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    public CreateServiceInstanceRequest createServiceInstanceRequest() {
        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(SD_ID, PLAN_ID, ORG_GUID, SPACE_GUID, getParameters());
        req.withServiceInstanceId(SI_ID);
        return req;
    }

    @Bean
    @Autowired
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
    @Autowired
    public ServiceBinding serviceBinding(CreateServiceInstanceBindingRequest req) {
        return new ServiceBinding(req);
    }

    @Bean
    public DeleteServiceInstanceBindingRequest deleteBindingRequest() {
        return new DeleteServiceInstanceBindingRequest(SI_ID, SB_ID, SD_ID, PLAN_ID,
                catalogService.getServiceDefinition(SD_ID));
    }

    @Bean
    UpdateServiceInstanceRequest updateServiceInstanceRequest() {
        UpdateServiceInstanceRequest req = new UpdateServiceInstanceRequest(SD_ID, PLAN_ID, getParameters());
        req.withServiceDefinition(catalogService.getServiceDefinition(SD_ID));
        req.withServiceInstanceId(SI_ID);
        return req;
    }

    @Bean
    DeleteServiceInstanceRequest deleteServiceInstanceRequest() {
        DeleteServiceInstanceRequest req = new DeleteServiceInstanceRequest(SI_ID, SD_ID, PLAN_ID,
                catalogService.getServiceDefinition(SD_ID));
        return req;
    }
}