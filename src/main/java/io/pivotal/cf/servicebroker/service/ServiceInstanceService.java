package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.persistance.ServiceInstance;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ServiceInstanceService implements org.springframework.cloud.servicebroker.service.ServiceInstanceService {

    private static final Logger LOG = Logger
            .getLogger(ServiceInstanceService.class);

    public static final String OBJECT_ID = "ServiceInstance";

    @Autowired
    private CatalogService catalogService;

    @Resource(name = "siTemplate")
    private HashOperations<String, String, ServiceInstance> repository;

    ServiceInstance getServiceInstance(String id) {
        if (id == null || getInstance(id) == null) {
            LOG.warn("service instance with id: " + id + " not found!");
            return null;
        }

        return getInstance(id);
    }

    @Override
    public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request)
            throws ServiceInstanceExistsException {

        if (getInstance(request.getServiceInstanceId()) != null) {
            throw new ServiceInstanceExistsException(request.getServiceInstanceId(), request.getServiceDefinitionId());
        }

        ServiceDefinition sd = catalogService.getServiceDefinition(request
                .getServiceDefinitionId());

        if (sd == null) {
            throw new ServiceBrokerException(
                    "Unable to find service definition with id: "
                            + request.getServiceDefinitionId());
        }

        LOG.info("creating service instance: " + request.getServiceInstanceId()
                + " service definition: " + request.getServiceDefinitionId());

        //TODO work to create service stuff
        ServiceInstance instance = new ServiceInstance(request);
        instance = saveInstance(instance);

        LOG.info("registered service instance: "
                + instance.getId());

        return new CreateServiceInstanceResponse().withAsync(false);
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest getLastServiceOperationRequest) {
        return null;
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {

        LOG.info("starting service instance delete: " + request.getServiceInstanceId());

        ServiceInstance instance = getInstance(request.getServiceInstanceId());
        if (instance == null) {
            throw new ServiceBrokerException("Service instance: "
                    + request.getServiceInstanceId() + " not found.");
        }

        LOG.info("requesting delete: " + request.getServiceInstanceId());

        //TODO work to delete the service

        deleteInstance(instance);
        return new DeleteServiceInstanceResponse().withAsync(false);
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) throws ServiceInstanceUpdateNotSupportedException {
        throw new ServiceInstanceUpdateNotSupportedException(
                "vRealize services are not updatable.");
    }

    private ServiceInstance getInstance(String id) {
        if (id == null) {
            return null;
        }
        return repository.get(OBJECT_ID, id);
    }

    ServiceInstance deleteInstance(ServiceInstance instance) {
        LOG.info("deleting service instance from repo: " + instance.getId());
        repository.delete(OBJECT_ID, instance.getId());
        return instance;
    }

    ServiceInstance saveInstance(io.pivotal.cf.servicebroker.persistance.ServiceInstance instance) {
        LOG.info("saving service instance to repo: " + instance.getId());
        repository.put(OBJECT_ID, instance.getId(), instance);
        return instance;
    }
}