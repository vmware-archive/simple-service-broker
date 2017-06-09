/*
 * Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NonNull;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@RedisHash("instances")
public class ServiceInstance implements Serializable {

    public static final String DELETE_REQUEST_ID = "DELETE_REQUEST_ID";

    public static final long serialVersionUID = 1L;

    @JsonSerialize
    @JsonProperty("id")
    @Id
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
    @JsonProperty("lastOperation")
    private LastOperation lastOperation;

    @JsonSerialize
    @JsonProperty("accepts_incomplete")
    private boolean acceptsIncomplete = false;

    @JsonSerialize
    @JsonProperty("deleted")
    private boolean deleted = false;

    public ServiceInstance() {
        super();
    }

    //TODO deal with stuff in response bodies
    public ServiceInstance(CreateServiceInstanceRequest request) {
        this();
        this.id = request.getServiceInstanceId();
        this.organizationGuid = request.getOrganizationGuid();
        this.planId = request.getPlanId();
        this.serviceId = request.getServiceDefinitionId();
        this.spaceGuid = request.getSpaceGuid();

        if (request.getParameters() != null) {
            this.parameters.putAll(request.getParameters());
        }
    }

    public ServiceInstance(UpdateServiceInstanceRequest request) {
        this();
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.serviceId = request.getServiceDefinitionId();
        if (request.getParameters() != null) {
            this.parameters.putAll(request.getParameters());
        }
    }

    public void addParameter(@NonNull String key, @NonNull Object value) {
        this.parameters.put(key, value);
    }

    public Object getParameter(@NonNull String key) {
        return this.parameters.get(key);
    }

    public LastOperation getLastOperation() {
        return this.lastOperation;
    }

    public void setLastOperation(LastOperation lastOperation) {
        this.lastOperation = lastOperation;
    }

    public CreateServiceInstanceResponse getCreateResponse() {
        CreateServiceInstanceResponse resp = new CreateServiceInstanceResponse();
        resp.withAsync(this.acceptsIncomplete);
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

    private boolean isInState(@NonNull String state) {
        lastOperationSanity();

        return getLastOperation().getState().equals(state);
    }

    private boolean isOperation(@NonNull String operation) {
        lastOperationSanity();

        return getLastOperation().getOperation().equals(operation);
    }

    public boolean isInProgress() {
        return isInState(LastOperation.IN_PROGRESS);
    }

    public boolean isFailed() {
        return isInState(LastOperation.FAILED);
    }

    public boolean isSuccessful() {
        return isInState(LastOperation.SUCCEEDED);
    }

    public boolean isCreate() {
        return isOperation(LastOperation.CREATE);
    }

    public boolean isDelete() {
        return isOperation(LastOperation.DELETE);
    }

    private void lastOperationSanity() {
        if (getLastOperation() == null || getLastOperation().getState() == null || getLastOperation().getOperation() == null) {
            throw new ServiceBrokerException("instance has no last operation.");
        }
    }
}