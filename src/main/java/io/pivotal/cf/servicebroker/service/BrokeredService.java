package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

public interface BrokeredService {

    void create(ServiceInstance instance) throws ServiceBrokerException;

    void delete(ServiceInstance instance) throws ServiceBrokerException;

    void update(ServiceInstance instance) throws ServiceBrokerException;

    boolean isAsynch();

}