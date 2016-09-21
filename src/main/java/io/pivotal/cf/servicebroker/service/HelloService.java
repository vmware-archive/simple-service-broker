package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HelloService extends DefaultServiceImpl {

    @Override
    public void create(ServiceInstance instance) throws ServiceBrokerException {
        log.info("hello!, I am creating a service instance!");
    }

    @Override
    public void delete(ServiceInstance instance) throws ServiceBrokerException {
        log.info("hello!, I am deleting a service instance!");
    }

    @Override
    public void update(ServiceInstance instance) throws ServiceBrokerException {
        log.info("hello!, I am updating a service instance!");
    }
}
