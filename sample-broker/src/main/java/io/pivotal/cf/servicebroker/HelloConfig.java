package io.pivotal.cf.servicebroker;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("cloud")
public class HelloConfig {

    @Bean
    public HelloBrokerRepository helloRepository(Environment env) {
        return Feign.builder()
                .encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(HelloBrokerRepository.class,
                        "http://" + env.getProperty("HELLO_HOST") + ":" + env.getProperty("HELLO_PORT"));
    }
}