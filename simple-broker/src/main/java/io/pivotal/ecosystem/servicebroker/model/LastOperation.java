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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastOperation implements Serializable {

    public static final long serialVersionUID = 1L;

    public static String CREATE = "create";
    public static String DELETE = "delete";
    public static String UPDATE = "update";
    public static String BIND = "bind";
    public static String UNBIND = "unbind";

    public static String SUCCEEDED = "succeeded";
    public static String FAILED = "failed";
    public static String IN_PROGRESS = "in progress";


    private String operation;
    private String state;
    private String description;

    public GetLastServiceOperationResponse toResponse() {
        GetLastServiceOperationResponse getLastServiceOperationResponse = new GetLastServiceOperationResponse();
        getLastServiceOperationResponse.withDescription(getDescription());
        getLastServiceOperationResponse.withDeleteOperation(isDelete());

        //work around for bug in OperationState.valueOf()
        if (SUCCEEDED.equals(getState())) {
            getLastServiceOperationResponse.withOperationState(OperationState.SUCCEEDED);
        } else if (FAILED.equals(getState())) {
            getLastServiceOperationResponse.withOperationState(OperationState.FAILED);
        } else if (IN_PROGRESS.equals(getState())) {
            getLastServiceOperationResponse.withOperationState(OperationState.IN_PROGRESS);
        }

        return getLastServiceOperationResponse;
    }

    private boolean isDelete() {
        return DELETE.equals(operation);
    }
}
