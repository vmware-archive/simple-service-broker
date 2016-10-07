package io.pivotal.cf.service.connector;

import feign.Feign;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloRepositoryFactory {

    public HelloRepository create(HelloServiceInfo info) {
        log.info("creating helloRepository with info: " + info);

        return Feign.builder()
                .errorDecoder(new HelloErrorDecoder())
                .target(HelloRepository.class, info.getUri());
    }
}
