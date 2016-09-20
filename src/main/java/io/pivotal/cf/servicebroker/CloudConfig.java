package io.pivotal.cf.servicebroker;

import io.pivotal.cf.servicebroker.persistance.ServiceInstance;
import io.pivotal.cf.servicebroker.persistance.ServiceInstanceBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Profile("cloud")
public class CloudConfig {

    @Autowired
    private Environment env;

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion("2.7");
    }

//    @Bean
//    Creds creds() {
//        return new Creds(env.getProperty("VRA_USER_ID"),
//                env.getProperty("VRA_USER_PASSWORD"),
//                env.getProperty("VRA_TENANT"));
//    }

    @Bean
    String serviceUri() {
        return env.getProperty("SERVICE_URI");
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    @Autowired
    @Qualifier("siTemplate")
    RedisTemplate<String, ServiceInstance> siTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, ServiceInstance> template = new RedisTemplate<String, ServiceInstance>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }

    @Bean
    @Autowired
    @Qualifier("sibTemplate")
    RedisTemplate<String, ServiceInstanceBinding> sibTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, ServiceInstanceBinding> template = new RedisTemplate<String, ServiceInstanceBinding>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }
}