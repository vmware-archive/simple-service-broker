package io.pivotal.cf.servicebroker.persistance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServiceInstance implements Serializable {

    public static final long serialVersionUID = 1L;

    @JsonSerialize
    @JsonProperty("service_instance_id")
    private String id;

    @JsonSerialize
    @JsonProperty("parameters")
    private final Map<String, Object> parameters = new HashMap<>();

    @JsonSerialize
    @JsonProperty("metadata")
    private final Map<String, Object> metadata = new HashMap<>();

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceDefinitionId;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    public ServiceInstance() {
        this(new CreateServiceInstanceRequest());
    }

    public ServiceInstance(CreateServiceInstanceRequest request) {
        this.serviceDefinitionId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.id = request.getServiceInstanceId();

        if (request.getParameters() != null) {
            getParameters().putAll(request.getParameters());
        }
    }

    public ServiceInstance(DeleteServiceInstanceRequest request) {
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.serviceDefinitionId = request.getServiceDefinitionId();
    }

    public ServiceInstance(UpdateServiceInstanceRequest request) {
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    public String getPlanId() {
        return planId;
    }
}