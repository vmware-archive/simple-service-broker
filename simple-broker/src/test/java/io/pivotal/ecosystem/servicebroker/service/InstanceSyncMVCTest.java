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

package io.pivotal.ecosystem.servicebroker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.ecosystem.servicebroker.model.LastOperation;
import io.pivotal.ecosystem.servicebroker.model.Operation;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.controller.ServiceInstanceController;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceSyncMVCTest {

    private static final String ID = "deleteme";

    private MockMvc mockMvc;

    @Autowired
    private DefaultServiceImpl mockDefaultServiceImpl;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    private InstanceService instanceService;

    @Before
    public void setUp() {
        ServiceInstance serviceInstance = TestConfig.getServiceInstance(ID, false);
        LastOperation lastOperation = new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created.");
        serviceInstance.setLastOperation(lastOperation);
        when(mockDefaultServiceImpl.createInstance(serviceInstance)).thenReturn(lastOperation);
        when(mockDefaultServiceImpl.lastOperation(serviceInstance)).thenReturn(lastOperation);
        when(serviceInstanceRepository.findOne(ID)).thenReturn(serviceInstance);
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);

        mockMvc = MockMvcBuilders.standaloneSetup(new ServiceInstanceController(catalogService, instanceService))
                .build();
    }

    @Test
    public void testSyncHappyPath() throws Exception {
        //create already "happened" in setup()

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        this.mockMvc.perform(put("/v2/service_instances/bogus")
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        this.mockMvc.perform(patch("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.updateRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        this.mockMvc.perform(delete("/v2/service_instances/" + ID + "?service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID)
                .content(toJson(TestConfig.deleteRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testNoLastOpCreateUpdateDelete() throws Exception {
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(null);
        this.mockMvc.perform(put("/v2/service_instances/bogus")
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());

        this.mockMvc.perform(patch("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.updateRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());

        this.mockMvc.perform(delete("/v2/service_instances/" + ID + "?service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID)
                .content(toJson(TestConfig.deleteRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    public void testBogusIdUpdateDelete() throws Exception {
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        this.mockMvc.perform(patch("/v2/service_instances/bogus")
                .content(toJson(TestConfig.updateRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());

        this.mockMvc.perform(delete("/v2/service_instances/bogus?service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID)
                .content(toJson(TestConfig.deleteRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone())
                .andDo(print());
    }

    @Test
    public void testAsyncRequestToSyncBroker() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        this.mockMvc.perform(put("/v2/service_instances/bogus?accepts_incomplete=true")
                .content(toJson(TestConfig.createRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    public void testSyncLastOperation() throws Exception {
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation?&service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testDuplicateCreate() throws Exception {
        //one instance already "created" in setup()
        this.mockMvc.perform(put("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    public static String toJson(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
