package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.Map;

public interface BrokeredService {

    void createInstance(ServiceInstance instance) throws ServiceBrokerException;

    void deleteInstance(ServiceInstance instance) throws ServiceBrokerException;

    void updateInstance(ServiceInstance instance) throws ServiceBrokerException;

    void createBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException;

    void deleteBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException;

    Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException;

    boolean isAsync();
}