package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.model.ServiceInstanceBinding;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class BindingService implements
        org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService {

    private static final Logger LOG = Logger
            .getLogger(BindingService.class);

    public static final String OBJECT_ID = "ServiceInstanceBinding";

    @Autowired
    private InstanceService instanceService;

    @Resource(name = "sibTemplate")
    private HashOperations<String, String, ServiceInstanceBinding> repository;

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
            CreateServiceInstanceBindingRequest request)
            throws ServiceInstanceBindingExistsException,
            ServiceBrokerException {

        String bindingId = request.getBindingId();

        ServiceInstanceBinding sib = repository.get(OBJECT_ID, bindingId);
        if (sib != null) {
            throw new ServiceInstanceBindingExistsException(request.getServiceInstanceId(), bindingId);
        }

        String serviceInstanceId = request.getServiceInstanceId();
        ServiceInstance si = instanceService
                .getServiceInstance(serviceInstanceId);

        if (si == null) {
            throw new ServiceBrokerException("service instance for binding: "
                    + bindingId + " is missing.");
        }

        LOG.info("creating binding for service instance: "
                + request.getServiceInstanceId() + " service: "
                + request.getServiceInstanceId());

        //TODO get credentials
        Map<String, Object> creds = new HashMap<>();
        ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId,
                serviceInstanceId, creds, null,
                request.getBindResource());

        LOG.info("saving binding: " + binding.getId());

        repository.put(OBJECT_ID, binding.getId(), binding);

        return new CreateServiceInstanceAppBindingResponse().withCredentials(creds);
    }

    @Override
    public void deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest request) {

        ServiceInstanceBinding binding = repository.get(OBJECT_ID, request
                .getBindingId());

        if (binding == null) {
            throw new ServiceBrokerException("binding with id: "
                    + request.getBindingId() + " does not exist.");
        }

        LOG.info("deleting binding for service instance: "
                + request.getBindingId() + " service instance: "
                + request.getServiceInstanceId());

        repository.delete(OBJECT_ID, binding.getId());
    }
}