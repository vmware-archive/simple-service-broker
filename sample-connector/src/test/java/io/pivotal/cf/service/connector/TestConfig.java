package io.pivotal.cf.service.connector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:test.properties")
class TestConfig {

    @Autowired
    private Environment env;

    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate();
    }

    @Bean
    public HelloServiceInfo helloServiceInfo() {
        Map<String, Object> m = new HashMap<>();
        Map<String, Object> creds = new HashMap<>();
        creds.put("username", env.getProperty("username"));
        creds.put("password", env.getProperty("password"));
        creds.put("host", env.getProperty("host"));
        creds.put("port", env.getProperty("port"));
        m.put("credentials", creds);
        HelloServiceInfoCreator hsic = new HelloServiceInfoCreator();
        return hsic.createServiceInfo(m);
    }

    @Bean
    @Autowired
    public HelloRepository helloRepository(HelloServiceInfo helloServiceInfo) {
        HelloRepositoryFactory hrf = new HelloRepositoryFactory();
        return hrf.create(helloServiceInfo);
    }
}