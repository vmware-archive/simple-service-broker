package io.pivotal.cf.servicebroker;

import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class TestConfig {

    public static final String SD_ID = "aUniqueId";

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

    public static String getContents(String fileName) throws Exception {
        URI u = new ClassPathResource(fileName).getURI();
        return new String(Files.readAllBytes(Paths.get(u)));
    }

    private static CreateServiceInstanceRequest getCreateServiceInstanceRequest() {
        Map<String, Object> parms = new HashMap<>();

        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
                SD_ID, "pId", "orgId", "spaceId", parms);
        req.withServiceInstanceId("anID");
        return req;
    }

    public static CreateServiceInstanceRequest getCreateServiceInstanceRequest(
            ServiceDefinition sd, boolean includeParms) {

        Map<String, Object> parms = new HashMap<>();

        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
                sd.getId(), sd.getPlans().get(0).getId(), "testOrgId",
                "testSpaceId", parms);
        req.withServiceInstanceId("anID");
        return req;
    }

    public static ServiceInstance getServiceInstance() {
        return new ServiceInstance(getCreateServiceInstanceRequest());
    }

    private static CreateServiceInstanceBindingRequest getCreateBindingRequest() {
        CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest("aSdId", "aPlanId", "anAppGuid", null, null);
        req.withBindingId("12345");
        return req;
    }

    public static ServiceBinding getServiceInstanceBinding() {
        return new ServiceBinding(getCreateBindingRequest());
    }
}