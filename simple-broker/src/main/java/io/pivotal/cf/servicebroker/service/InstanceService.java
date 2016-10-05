package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceDefinitionDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstanceService implements ServiceInstanceService {

    private static final String OBJECT_ID = "Instance";

    public InstanceService(CatalogService catalogService, BrokeredService brokeredService,
                           HashOperations<String, String, ServiceInstance> instanceTemplate) {
        this.catalogService = catalogService;
        this.brokeredService = brokeredService;
        this.instanceTemplate = instanceTemplate;
    }

    private CatalogService catalogService;

    private BrokeredService brokeredService;

    private HashOperations<String, String, ServiceInstance> instanceTemplate;

    ServiceInstance getServiceInstance(String id) {
        if (id == null || getInstance(id) == null) {
            log.warn("service instance with id: " + id + " not found!");
            return null;
        }

        return getInstance(id);
    }

    @Override
    public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {

        try {
            if (getInstance(request.getServiceInstanceId()) != null) {
                throw new ServiceInstanceExistsException(request.getServiceInstanceId(), request.getServiceInstanceId());
            }
        } catch (ServiceInstanceDoesNotExistException e) {
            //ok, don't have this instance, keep going
        }

        ServiceDefinition sd = catalogService.getServiceDefinition(request.getServiceDefinitionId());

        if (sd == null) {
            throw new ServiceDefinitionDoesNotExistException(request.getServiceDefinitionId());
        }

        log.info("creating service instance: " + request.getServiceInstanceId() + " service definition: " + request.getServiceDefinitionId());
        ServiceInstance instance = new ServiceInstance(request);
        brokeredService.createInstance(instance);
        saveInstance(instance);

        log.info("registered service instance: " + request.getServiceInstanceId());
        return instance.getCreateResponse();
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest getLastServiceOperationRequest) {
        //TODO deal with Async
        return null;
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {

        log.info("starting service instance delete: " + request.getServiceInstanceId());
        ServiceInstance instance = getInstance(request.getServiceInstanceId());
        brokeredService.deleteInstance(instance);
        deleteInstance(instance);

        log.info("deleted service instance: " + request.getServiceInstanceId());
        return instance.getDeleteResponse();
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
        log.info("starting service instance update: " + request.getServiceInstanceId());
        ServiceInstance originalInstance = getInstance(request.getServiceInstanceId());
        ServiceInstance updatedInstance = new ServiceInstance(request);

        originalInstance.setServiceId(updatedInstance.getServiceId());
        originalInstance.setPlanId(updatedInstance.getPlanId());
        originalInstance.setParameters(updatedInstance.getParameters());

        brokeredService.updateInstance(originalInstance);
        saveInstance(originalInstance);

        log.info("updated service instance: " + request.getServiceInstanceId());
        return originalInstance.getUpdateResponse();
    }

    private ServiceInstance getInstance(String id) throws ServiceBrokerException {
        if (id == null) {
            throw new ServiceBrokerException("null serviceInstanceId");
        }

        ServiceInstance si = instanceTemplate.get(OBJECT_ID, id);

        if (si == null) {
            throw new ServiceInstanceDoesNotExistException(id);
        }

        return si;
    }

    private ServiceInstance deleteInstance(ServiceInstance instance) {
        log.info("deleting service instance from repo: " + instance.getId());
        instanceTemplate.delete(OBJECT_ID, instance.getId());
        return instance;
    }

    private ServiceInstance saveInstance(io.pivotal.cf.servicebroker.model.ServiceInstance instance) {
        log.info("saving service instance to repo: " + instance.getId());
        instanceTemplate.put(OBJECT_ID, instance.getId(), instance);
        return instance;
    }
}