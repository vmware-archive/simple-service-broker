package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

public class DefaultServiceImpl implements BrokeredService {

    @Override
    public void create(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public void delete(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public void update(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public boolean isAsynch() {
        return false;
    }
}
