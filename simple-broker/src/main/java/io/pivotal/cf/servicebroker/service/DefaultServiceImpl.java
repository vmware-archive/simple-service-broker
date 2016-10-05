package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.HashMap;
import java.util.Map;

public class DefaultServiceImpl implements BrokeredService {

    @Override
    public void createInstance(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public void deleteInstance(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public void updateInstance(ServiceInstance instance) throws ServiceBrokerException {
    }

    @Override
    public void createBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {

    }

    @Override
    public void deleteBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {

    }

    @Override
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        return new HashMap<>();
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}
