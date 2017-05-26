package io.pivotal.ecosystem.servicebroker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.ecosystem.servicebroker.model.LastOperation;
import io.pivotal.ecosystem.servicebroker.model.Operation;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.controller.ServiceInstanceController;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceMVCTest {

    private static final String ID = "deleteme";

    private MockMvc mockMvc;

    @Autowired
    private DefaultServiceImpl mockDefaultServiceImpl;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ServiceInstanceController(catalogService, new InstanceService(catalogService, mockDefaultServiceImpl, serviceInstanceRepository)))
                .build();
    }

    @After
    public void cleanUp() {
        if (serviceInstanceRepository.findOne(ID) != null) {
            serviceInstanceRepository.delete(ID);
        }
    }

    @Test
    public void testSyncHappyPath() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);
        this.mockMvc.perform(put("/v2/service_instances/" + ID)
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
    public void testAsyncHappyPath() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(true);
        this.mockMvc.perform(put("/v2/service_instances/" + ID + "?accepts_incomplete=true")
                .content(toJson(TestConfig.createRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andDo(print());

        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation")
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
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation")
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
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone())
                .andDo(print());
    }

    @Test
    public void testNoLastOpCreateUpdateDelete() throws Exception {
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);
        this.mockMvc.perform(put("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());

        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);
        this.mockMvc.perform(put("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
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
    public void testAsyncCreateRefused() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "created."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(true);
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
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(true);
        this.mockMvc.perform(put("/v2/service_instances/" + ID + "?accepts_incomplete=true")
                .content(toJson(TestConfig.createRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

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
    public void testAsyncRequestToSyncBroker() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);
        this.mockMvc.perform(put("/v2/service_instances/" + ID + "?accepts_incomplete=true")
                .content(toJson(TestConfig.createRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    public void testAsyncFlagsMissing() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(true);
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
    public void testSyncLastOperation() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);
        this.mockMvc.perform(put("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testAsyncLastOperation() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(true);
        this.mockMvc.perform(put("/v2/service_instances/" + ID + "?accepts_incomplete=true")
                .content(toJson(TestConfig.createRequest(ID, true)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        when(mockDefaultServiceImpl.lastOperation(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.IN_PROGRESS, "creating."));
        this.mockMvc.perform(get("/v2/service_instances/" + ID + "/last_operation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testDuplicateCreate() throws Exception {
        when(mockDefaultServiceImpl.createInstance(any(ServiceInstance.class))).thenReturn(new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, "created."));
        when(mockDefaultServiceImpl.isAsync()).thenReturn(false);
        this.mockMvc.perform(put("/v2/service_instances/" + ID)
                .content(toJson(TestConfig.createRequest(ID, false)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());

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
