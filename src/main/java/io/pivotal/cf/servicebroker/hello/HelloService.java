package io.pivotal.cf.servicebroker.hello;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class HelloService extends DefaultServiceImpl {

    @Override
    public void createInstance(ServiceInstance instance) throws ServiceBrokerException {
        log.info("hello!, I am creating a service instance!");
    }

    @Override
    public void deleteInstance(ServiceInstance instance) throws ServiceBrokerException {
        log.info("hello!, I am deleting a service instance!");
    }

    @Override
    public void updateInstance(ServiceInstance instance) throws ServiceBrokerException {
        log.info("hello!, I am updating a service instance!");
    }

    @Override
    public void createBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        log.info("hello!, I am creating a binding!");
    }

    @Override
    public void deleteBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        log.info("hello!, I am deleting a binding!");
    }

    @Override
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        log.info("hello!, I am returning credentials!");
        Map<String, Object> m = new HashMap<>();
        m.put("host", "helloHost");
        m.put("port", "helloPort");
        m.put("username", "hello");
        m.put("password", "world");
        m.put("database", "helloDB");
        m.put("uri", "http://" + m.get("username") + ":" + m.get("password") + "@" + m.get("host") + ":" + m.get("port") + "/" + m.get("database"));

        return m;
    }

    @Override
    //TODO deal with async
    public boolean isAsynch() {
        return false;
    }
}