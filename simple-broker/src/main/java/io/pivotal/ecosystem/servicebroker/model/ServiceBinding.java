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
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@RedisHash("bindings")
public class ServiceBinding implements Serializable {

    public static final long serialVersionUID = 1L;

    @JsonSerialize
    @JsonProperty("id")
    @Id
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
    private final Map<String, Object> bindResource = new HashMap<>();

    @JsonSerialize
    @JsonProperty("parameters")
    private final Map<String, Object> parameters = new HashMap<>();

    @JsonSerialize
    @JsonProperty("credentials")
    private final Map<String, Object> credentials = new HashMap<>();

    @JsonSerialize
    @JsonProperty("deleted")
    private boolean deleted = false;

    @JsonSerialize
    @JsonProperty("lastOperation")
    private LastOperation lastOperation;

    public ServiceBinding() {
        super();
    }

    //TODO deal with stuff in response bodies
    public ServiceBinding(CreateServiceInstanceBindingRequest request) {
        this();
        this.id = request.getBindingId();
        this.serviceId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.appGuid = request.getBoundAppGuid();

        if (request.getBindResource() != null) {
            bindResource.putAll(request.getBindResource());
        }

        if (request.getParameters() != null) {
            this.parameters.putAll(request.getParameters());
        }
    }

    public Object getParameter(@NonNull String key) {
        return this.parameters.get(key);
    }

    public CreateServiceInstanceAppBindingResponse getCreateResponse() {
        CreateServiceInstanceAppBindingResponse resp = new CreateServiceInstanceAppBindingResponse();
        resp.withCredentials(credentials);
        return resp;
    }
}