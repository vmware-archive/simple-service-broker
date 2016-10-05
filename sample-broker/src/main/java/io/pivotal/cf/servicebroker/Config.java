package io.pivotal.cf.servicebroker;

import feign.Feign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("cloud")
public class Config {

    @Bean
    public HelloBrokerRepository helloRepository(Environment env) {
        return Feign.builder().target(HelloBrokerRepository.class,
                "http://" + env.getProperty("hostname") + ":" + env.getProperty("port"));
    }
}