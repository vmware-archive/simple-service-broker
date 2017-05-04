/**
 * vrealize-service-broker
 * <p>
 * Copyright (c) 2015-Present Pivotal Software, Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

package io.pivotal.ecosystem.servicebroker.model;

import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;

import java.io.Serializable;

public class LastOperation implements Serializable {

    public static final long serialVersionUID = 1L;

    private OperationState state;

    private String description;

    private boolean isDelete;

    public static LastOperation fromResponse(GetLastServiceOperationResponse response) {
        return new LastOperation(response.getState(), response.getDescription(), response.isDeleteOperation());
    }

    public LastOperation(OperationState state, String description, boolean isDelete) {
        setState(state);
        setDescription(description);
        setDelete(isDelete);
    }

    public OperationState getState() {
        return state;
    }

    public void setState(OperationState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private boolean isDelete() {
        return isDelete;
    }

    private void setDelete(boolean delete) {
        isDelete = delete;
    }

    public GetLastServiceOperationResponse toResponse() {
        return new GetLastServiceOperationResponse().
                withDescription(getDescription()).
                withOperationState(getState()).
                withDeleteOperation(isDelete());
    }
}
