package io.pivotal.cf.service.connector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.service.AbstractServiceConnectorCreator;
import org.springframework.cloud.service.ServiceConnectorConfig;

@Slf4j
public class HelloConnectionCreator extends AbstractServiceConnectorCreator<HelloRepository, HelloServiceInfo> {

    @Override
    public HelloRepository create(HelloServiceInfo serviceInfo, ServiceConnectorConfig serviceConnectorConfig) {
        log.debug("creating hello repo wth service info: " + serviceInfo);
        return new HelloRepositoryFactory().create(serviceInfo);
    }
}