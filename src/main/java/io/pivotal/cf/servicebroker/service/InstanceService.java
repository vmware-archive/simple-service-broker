package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

@Service
public class InstanceService implements org.springframework.cloud.servicebroker.service.ServiceInstanceService {

    private static final Logger LOG = Logger
            .getLogger(InstanceService.class);

    public static final String OBJECT_ID = "Instance";

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private BrokeredService brokeredService;

    private HashOperations<String, String, ServiceInstance> instanceTemplate;

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

        //TODO generalize, use right exceptions
        if (getInstance(request.getServiceInstanceId()) != null) {
            throw new ServiceInstanceExistsException(request.getServiceInstanceId(), request.getServiceDefinitionId());
        }

        ServiceDefinition sd = catalogService.getServiceDefinition(request.getServiceDefinitionId());

        if (sd == null) {
            throw new ServiceBrokerException("Unable to find service definition with id: " + request.getServiceDefinitionId());
        }

        LOG.info("creating service instance: " + request.getServiceInstanceId() + " service definition: " + request.getServiceDefinitionId());

        ServiceInstance instance = new ServiceInstance(request);
        brokeredService.create(instance);
        saveInstance(instance);

        LOG.info("registered service instance: " + instance.getId());

        return new CreateServiceInstanceResponse().withAsync(brokeredService.isAsynch());
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest getLastServiceOperationRequest) {
        //TODO deal with Async
        return null;
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {

        LOG.info("starting service instance delete: " + request.getServiceInstanceId());

        ServiceInstance instance = getInstance(request.getServiceInstanceId());
        if (instance == null) {
            throw new ServiceBrokerException("Service instance: " + request.getServiceInstanceId() + " not found.");
        }

        LOG.info("requesting delete: " + request.getServiceInstanceId());
        brokeredService.delete(instance);

        deleteInstance(instance);
        return new DeleteServiceInstanceResponse().withAsync(brokeredService.isAsynch());
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
        //TODO deal with updates
        LOG.info("starting service instance update: " + request.getServiceInstanceId());

        ServiceInstance instance = getInstance(request.getServiceInstanceId());
        if (instance == null) {
            throw new ServiceBrokerException("Service instance: " + request.getServiceInstanceId() + " not found.");
        }

        LOG.info("requesting update: " + request.getServiceInstanceId());
        brokeredService.update(instance);

        saveInstance(instance);
        return new UpdateServiceInstanceResponse().withAsync(brokeredService.isAsynch());
    }

    private ServiceInstance getInstance(String id) {
        if (id == null) {
            return null;
        }
        return instanceTemplate.get(OBJECT_ID, id);
    }

    ServiceInstance deleteInstance(ServiceInstance instance) {
        LOG.info("deleting service instance from repo: " + instance.getId());
        instanceTemplate.delete(OBJECT_ID, instance.getId());
        return instance;
    }

    ServiceInstance saveInstance(io.pivotal.cf.servicebroker.model.ServiceInstance instance) {
        LOG.info("saving service instance to repo: " + instance.getId());
        instanceTemplate.put(OBJECT_ID, instance.getId(), instance);
        return instance;
    }
}