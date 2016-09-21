package io.pivotal.cf.servicebroker.model;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.service.InstanceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class ServiceInstanceRepositoryTest {

    @Resource(name = "siTemplate")
    private HashOperations<String, String, ServiceInstance> repository;

    @Before
    public void setup() {
        Set<String> keys = repository.keys(InstanceService.OBJECT_ID);
        for (String key : keys) {
            repository.delete(InstanceService.OBJECT_ID, key);
        }
    }

    @After
    public void teardown() {
        Set<String> keys = repository.keys(InstanceService.OBJECT_ID);
        for (String key : keys) {
            repository.delete(InstanceService.OBJECT_ID, key);
        }
    }

    @Test
    public void instanceInsertedSuccessfully() throws Exception {
        ServiceInstance si = TestConfig.getServiceInstance();
        assertEquals(0, repository.entries(InstanceService.OBJECT_ID).size());

        repository.put(InstanceService.OBJECT_ID, si.getId(), si);
        assertEquals(1, repository.entries(InstanceService.OBJECT_ID).size());
    }

    @Test
    public void instanceDeletedSuccessfully() throws Exception {
        ServiceInstance si = TestConfig.getServiceInstance();
        assertEquals(0, repository.entries(InstanceService.OBJECT_ID).size());

        repository.put(InstanceService.OBJECT_ID, si.getId(), si);
        assertEquals(1, repository.entries(InstanceService.OBJECT_ID).size());

        ServiceInstance si2 = repository.get(InstanceService.OBJECT_ID, si.getId());
        assertNotNull(si2);
        assertEquals("anID", si2.getId());

        ServiceInstance si3 = repository.get(InstanceService.OBJECT_ID, "anID");
        assertNotNull(si3);
        assertEquals("anID", si3.getId());
        assertEquals(TestConfig.SD_ID, si3.getServiceDefinitionId());

        //System.out.println(gson.toJson(si3));

        repository.delete(InstanceService.OBJECT_ID, si3.getId());

        assertEquals(0, repository.entries(InstanceService.OBJECT_ID).size());
    }
}