package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class BindingService implements ServiceInstanceBindingService {

    private static final String OBJECT_ID = "binding";

    public BindingService(InstanceService instanceService, BrokeredService brokeredService, RedisTemplate<String, ServiceBinding> bindingTemplate) {
        this.instanceService = instanceService;
        this.brokeredService = brokeredService;
        this.bindingTemplate = bindingTemplate;
    }

    private InstanceService instanceService;

    private BrokeredService brokeredService;

    private RedisTemplate<String, ServiceBinding> bindingTemplate;

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) throws ServiceBrokerException {
        try {
            if (getBinding(request.getServiceInstanceId()) != null) {
                throw new ServiceInstanceBindingExistsException(request.getServiceInstanceId(), request.getServiceInstanceId());
            }
        } catch (ServiceInstanceBindingDoesNotExistException e) {
            //ok, don't have this binding, keep going
        }

        ServiceInstance instance = instanceService.getServiceInstance(request.getServiceInstanceId());

        log.info("creating binding for service instance: " + request.getServiceInstanceId() + " service: " + request.getServiceInstanceId());
        ServiceBinding binding = new ServiceBinding(request);

        brokeredService.createBinding(instance, binding);
        Map<String, Object> creds = brokeredService.getCredentials(instance, binding);
        binding.getCredentials().putAll(creds);

        log.info("saving binding: " + request.getBindingId());
        bindingTemplate.opsForHash().put(OBJECT_ID, request.getBindingId(), binding);

        return binding.getCreateResponse();
    }

    @Override
    public void deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest request) {

        ServiceBinding binding = getBinding(request.getBindingId());

        if (binding == null) {
            throw new ServiceBrokerException("binding with id: " + request.getBindingId() + " does not exist.");
        }

        String serviceInstanceId = request.getServiceInstanceId();
        ServiceInstance si = instanceService.getServiceInstance(serviceInstanceId);

        if (si == null) {
            throw new ServiceBrokerException("service instance for binding: " + request.getBindingId() + " is missing.");
        }

        brokeredService.deleteBinding(si, binding);

        log.info("deleting binding for service instance: " + request.getBindingId() + " service instance: " + request.getServiceInstanceId());
        bindingTemplate.opsForHash().delete(OBJECT_ID, binding.getId());
    }

    private ServiceBinding getBinding(String id) throws ServiceBrokerException {
        if (id == null) {
            throw new ServiceBrokerException("null serviceBindingId");
        }

        ServiceBinding sb = (ServiceBinding) bindingTemplate.opsForHash().get(OBJECT_ID, id);

        if (sb == null) {
            throw new ServiceInstanceBindingDoesNotExistException(id);
        }

        return sb;
    }
}