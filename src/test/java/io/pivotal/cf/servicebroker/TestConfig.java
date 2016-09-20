package io.pivotal.cf.servicebroker;

import io.pivotal.cf.servicebroker.persistance.ServiceInstance;
import io.pivotal.cf.servicebroker.persistance.ServiceInstanceBinding;
import io.pivotal.cf.servicebroker.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class TestConfig {

    public static final String SD_ID = "aUniqueId";

    @Autowired
    private Environment env;

    @Autowired
    private CatalogService catalogService;

    @Bean
    String serviceUri() {
        return env.getProperty("vRserviceUri");
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);
        factory.setUsePool(true);
        return factory;
    }


    @Bean
    @Qualifier("siTemplate")
    RedisTemplate<String, ServiceInstance> siTemplate() {
        RedisTemplate<String, ServiceInstance> template = new RedisTemplate<String, ServiceInstance>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    @Qualifier("sibTemplate")
    RedisTemplate<String, ServiceInstanceBinding> sibTemplate() {
        RedisTemplate<String, ServiceInstanceBinding> template = new RedisTemplate<String, ServiceInstanceBinding>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    public static String getContents(String fileName) throws Exception {
        URI u = new ClassPathResource(fileName).getURI();
        return new String(Files.readAllBytes(Paths.get(u)));
    }

    public static CreateServiceInstanceRequest getCreateServiceInstanceRequest() {
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

//	public static DeleteServiceInstanceRequest getDeleteServiceInstanceRequest() {
//		CreateServiceInstanceRequest creq = getCreateServiceInstanceRequest();
//		DeleteServiceInstanceRequest dreq = new DeleteServiceInstanceRequest(
//				creq.getServiceInstanceId(), creq.getServiceDefinitionId(),
//				creq.getPlanId(), true);
//		return dreq;
//	}

    public static ServiceInstance getServiceInstance() {
        return new ServiceInstance(getCreateServiceInstanceRequest());
    }

    public static CreateServiceInstanceBindingRequest getCreateBindingRequest() {
        io.pivotal.cf.servicebroker.persistance.ServiceInstance si = getServiceInstance();
        CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest(
                si.getServiceDefinitionId(), si.getPlanId(), "anAppId",
                si.getParameters());
        req.withBindingId("98765");
        req.withServiceInstanceId(si.getId());
        return req;
    }

    public static ServiceInstanceBinding getServiceInstanceBinding()
            throws ServiceBrokerException {
        CreateServiceInstanceBindingRequest req = getCreateBindingRequest();
        return new ServiceInstanceBinding(req.getBindingId(),
                req.getServiceInstanceId(), null, null, null);
    }

    public DeleteServiceInstanceBindingRequest getDeleteBindingRequest() {
        ServiceInstance si = getServiceInstance();
        CreateServiceInstanceBindingRequest creq = getCreateBindingRequest();
        return new DeleteServiceInstanceBindingRequest(
                si.getId(),
                creq.getBindingId(), si.getServiceDefinitionId(),
                si.getPlanId(), catalogService.getServiceDefinition(si.getServiceDefinitionId()));
    }
}