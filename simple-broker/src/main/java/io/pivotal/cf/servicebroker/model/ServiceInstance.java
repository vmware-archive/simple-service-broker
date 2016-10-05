package io.pivotal.cf.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.cloud.servicebroker.model.*;

import java.io.Serializable;
import java.util.Map;

@Data
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
    private Map<String, Object> parameters;

    @JsonSerialize
    @JsonProperty("accepts_incomplete")
    private boolean acceptsIncomplete;

    //TODO deal with stuff in response bodies
    public ServiceInstance(CreateServiceInstanceRequest request) {
        super();
        this.id = request.getServiceInstanceId();
        this.organizationGuid = request.getOrganizationGuid();
        this.planId = request.getPlanId();
        this.serviceId = request.getServiceDefinitionId();
        this.spaceGuid = request.getSpaceGuid();
        this.parameters = request.getParameters();
    }

    public ServiceInstance(UpdateServiceInstanceRequest request) {
        super();
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.serviceId = request.getServiceDefinitionId();
        this.parameters = request.getParameters();
    }

    public CreateServiceInstanceResponse getCreateResponse() {
        CreateServiceInstanceResponse resp = new CreateServiceInstanceResponse();
        resp.withAsync(this.acceptsIncomplete);
        if (this.parameters.containsKey("dashboard_url")) {
            resp.withDashboardUrl(this.parameters.get("dashboard_url").toString());
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