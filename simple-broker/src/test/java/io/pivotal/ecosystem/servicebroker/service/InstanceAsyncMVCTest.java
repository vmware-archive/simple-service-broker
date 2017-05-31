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
public class InstanceAsyncMVCTest {

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
        ServiceInstance serviceInstance = TestConfig.getServiceInstance(ID, true);
        LastOperation lastOperation = new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating.");
        serviceInstance.setLastOperation(lastOperation);
        when(mockDefaultServiceImpl.createInstance(serviceInstance)).thenReturn(lastOperation);
        when(mockDefaultServiceImpl.lastOperation(serviceInstance)).thenReturn(lastOperation);
        when(serviceInstanceRepository.findOne(ID)).thenReturn(serviceInstance);
        when(mockDefaultServiceImpl.isAsync()).thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(new ServiceInstanceController(catalogService, instanceService))
                .build();
    }

    @Test
    public void testAsyncHappyPath() throws Exception {
        //create has been mocked up out of existence...

        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation?&service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.IN_PROGRESS, "updating."));
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        this.mockMvc.perform(patch("/v2/service_instances/" + ID + "?accepts_incomplete=true")
                .content(toJson(TestConfig.updateRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation?&service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.IN_PROGRESS, "deleting."));
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        this.mockMvc.perform(delete("/v2/service_instances/" + ID + "?service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID + "&accepts_incomplete=true")
                .content(toJson(TestConfig.deleteRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation?&service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone())
                .andDo(print());
    }

    @Test
    public void testAsyncCreateRefused() throws Exception {
        this.mockMvc.perform(put("/v2/service_instances/" + ID + "?accepts_incomplete=false")
                .content(toJson(TestConfig.createRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());

        this.mockMvc.perform(put("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.createRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    public void testConcurrentInProgressFails() throws Exception {
        when(mockDefaultServiceImpl.updateInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, "updated."));
        this.mockMvc.perform(patch("/v2/service_instances/" + ID + "?accepts_incomplete=true")
                .content(toJson(TestConfig.updateRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andDo(print());

        when(mockDefaultServiceImpl.deleteInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, "deleted."));
        this.mockMvc.perform(delete("/v2/service_instances/" + ID + "?service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID + "&accepts_incomplete=true")
                .content(toJson(TestConfig.deleteRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

    @Test
    public void testAsyncFlagsMissing() throws Exception {
        this.mockMvc.perform(put("/v2/service_instances/" + ID + "?accepts_incomplete=false")
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());

        this.mockMvc.perform(put("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    public void testAsyncLastOperation() throws Exception {
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation?&service_id=" + TestConfig.SD_ID + "&plan_id=" + TestConfig.PLAN_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    public static String toJson(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
