package io.pivotal.cf.servicebroker;

import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Profile("cloud")
public class Config {

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion("2.10");
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    @Autowired
    RedisTemplate<String, ServiceInstance> instanceTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, ServiceInstance> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }

    @Bean
    @Autowired
    RedisTemplate<String, ServiceBinding> bindingTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, ServiceBinding> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }
}