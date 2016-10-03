package io.pivotal.cf.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.cloud.servicebroker.model.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServiceInstance implements Serializable {

    public static final long serialVersionUID = 1L;

    @JsonSerialize
    @JsonProperty("id")
    private String id;

    @JsonSerialize
    @JsonProperty("organization_guid")
    private String organizationGuid;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceId;

    @JsonSerialize
    @JsonProperty("space_guid")
    private String spaceGuid;

    @JsonSerialize
    @JsonProperty("parameters")
    private final Map<String, Object> parameters = new HashMap<>();

    @JsonSerialize
    @JsonProperty("accepts_incomplete")
    private boolean acceptsIncomplete;

    //TODO deal with stuff in response bodies
    public ServiceInstance(CreateServiceInstanceRequest request) {
        super();
        this.id = request.getServiceInstanceId();
        this.organizationGuid = request.getOrganizationGuid();
        setPlanId(request.getPlanId());
        setServiceId(request.getServiceDefinitionId());
        this.spaceGuid = request.getSpaceGuid();
        setParameters(request.getParameters());
    }

    public ServiceInstance(UpdateServiceInstanceRequest request) {
        super();
        this.id = request.getServiceInstanceId();
        setPlanId(request.getPlanId());
        setServiceId(request.getServiceDefinitionId());
        setParameters(request.getParameters());
    }

    public void setParameters(Map<String, Object> m) {
        if (m != null) {
            getParameters().clear();
            getParameters().putAll(m);
        }
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public String getId() {
        return id;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String s) {
        this.serviceId = s;
    }

    public String getPlanId() {
        return this.planId;
    }

    public void setPlanId(String s) {
        this.planId = s;
    }

    public CreateServiceInstanceResponse getCreateResponse() {
        CreateServiceInstanceResponse resp = new CreateServiceInstanceResponse();
        resp.withAsync(this.acceptsIncomplete);
        if (getParameters().containsKey("dashboard_url")) {
            resp.withDashboardUrl(getParameters().get("dashboard_url").toString());
        }
        return resp;
    }

    public DeleteServiceInstanceResponse getDeleteResponse() {
        DeleteServiceInstanceResponse resp = new DeleteServiceInstanceResponse();
        resp.withAsync(this.acceptsIncomplete);
        return resp;
    }

    public UpdateServiceInstanceResponse getUpdateResponse() {
        UpdateServiceInstanceResponse resp = new UpdateServiceInstanceResponse();
        resp.withAsync(this.acceptsIncomplete);
        return resp;
    }
}