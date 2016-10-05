package io.pivotal.cf.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;

import java.io.Serializable;
import java.util.Map;

@Data
public class ServiceBinding implements Serializable {

    public static final long serialVersionUID = 1L;

    @JsonSerialize
    @JsonProperty("id")
    private String id;

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceId;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("app_guid")
    private String appGuid;

    @JsonSerialize
    @JsonProperty("bind_resource")
    private final Map<String, Object> bindResource;

    @JsonSerialize
    @JsonProperty("parameters")
    private final Map<String, Object> parameters;

    @JsonSerialize
    @JsonProperty("credentials")
    private Map<String, Object> credentials;

    //TODO deal with stuff in response bodies
    public ServiceBinding(CreateServiceInstanceBindingRequest request) {
        this.id = request.getBindingId();
        this.serviceId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.appGuid = request.getBoundAppGuid();
        this.bindResource = request.getBindResource();
        this.parameters = request.getParameters();
    }

    public CreateServiceInstanceAppBindingResponse getCreateResponse() {
        CreateServiceInstanceAppBindingResponse resp = new CreateServiceInstanceAppBindingResponse();
        resp.withCredentials(credentials);
        return resp;
    }
}