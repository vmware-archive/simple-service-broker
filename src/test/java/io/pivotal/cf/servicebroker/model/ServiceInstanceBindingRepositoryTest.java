package io.pivotal.cf.servicebroker.model;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.service.BindingService;
import io.pivotal.cf.servicebroker.service.InstanceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class ServiceInstanceBindingRepositoryTest {

    @Resource(name = "sibTemplate")
    private HashOperations<String, String, ServiceInstanceBinding> repository;

    @Resource(name = "siTemplate")
    private HashOperations<String, String, ServiceInstance> siRepository;

    @Before
    public void setup() {
        repository.delete(BindingService.OBJECT_ID, repository.entries(BindingService.OBJECT_ID));
        repository.delete(InstanceService.OBJECT_ID, repository.entries(InstanceService.OBJECT_ID));
    }

    @After
    public void teardown() {
        repository.delete(BindingService.OBJECT_ID, repository.entries(BindingService.OBJECT_ID));
        repository.delete(InstanceService.OBJECT_ID, repository.entries(InstanceService.OBJECT_ID));
    }

    @Test
    public void instanceInsertedSuccessfully() throws Exception {
        ServiceInstanceBinding sib = TestConfig.getServiceInstanceBinding();
        assertEquals(0, repository.entries(BindingService.OBJECT_ID).size());

        repository.put(BindingService.OBJECT_ID, sib.getId(), sib);
        assertEquals(1, repository.entries(BindingService.OBJECT_ID).size());
    }

    @Test
    public void instanceDeletedSuccessfully() throws Exception {
        ServiceInstanceBinding sib = TestConfig.getServiceInstanceBinding();
        assertEquals(0, repository.entries(BindingService.OBJECT_ID).size());

        repository.put(BindingService.OBJECT_ID, sib.getId(), sib);
        assertEquals(1, repository.entries(BindingService.OBJECT_ID).size());

        Map<String, ServiceInstanceBinding> m = repository.entries(BindingService.OBJECT_ID);
        assertEquals(1, m.size());

        ServiceInstanceBinding sib2 = repository.get(BindingService.OBJECT_ID, sib.getId());
        assertNotNull(sib2);
        assertEquals("anID", sib2.getServiceInstanceId());

        ServiceInstanceBinding sib3 = repository.get(BindingService.OBJECT_ID, "98765");
        assertNotNull(sib3);
        assertEquals("anID", sib3.getServiceInstanceId());
        assertEquals("98765", sib3.getId());
        assertNotNull(sib3.getCredentials());

        // System.out.println(gson.toJson(sib3));

        repository.delete(BindingService.OBJECT_ID, sib3.getId());

        assertEquals(0, repository.entries(BindingService.OBJECT_ID).size());
    }
}