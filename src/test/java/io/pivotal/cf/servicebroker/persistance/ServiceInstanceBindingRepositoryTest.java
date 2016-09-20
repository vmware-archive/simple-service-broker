package io.pivotal.cf.servicebroker.persistance;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.service.ServiceInstanceBindingService;
import io.pivotal.cf.servicebroker.service.ServiceInstanceService;
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
        repository.delete(ServiceInstanceBindingService.OBJECT_ID, repository.entries(ServiceInstanceBindingService.OBJECT_ID));
        repository.delete(ServiceInstanceService.OBJECT_ID, repository.entries(ServiceInstanceService.OBJECT_ID));
    }

    @After
    public void teardown() {
        repository.delete(ServiceInstanceBindingService.OBJECT_ID, repository.entries(ServiceInstanceBindingService.OBJECT_ID));
        repository.delete(ServiceInstanceService.OBJECT_ID, repository.entries(ServiceInstanceService.OBJECT_ID));
    }

    @Test
    public void instanceInsertedSuccessfully() throws Exception {
        ServiceInstanceBinding sib = TestConfig.getServiceInstanceBinding();
        assertEquals(0, repository.entries(ServiceInstanceBindingService.OBJECT_ID).size());

        repository.put(ServiceInstanceBindingService.OBJECT_ID, sib.getId(), sib);
        assertEquals(1, repository.entries(ServiceInstanceBindingService.OBJECT_ID).size());
    }

    @Test
    public void instanceDeletedSuccessfully() throws Exception {
        ServiceInstanceBinding sib = TestConfig.getServiceInstanceBinding();
        assertEquals(0, repository.entries(ServiceInstanceBindingService.OBJECT_ID).size());

        repository.put(ServiceInstanceBindingService.OBJECT_ID, sib.getId(), sib);
        assertEquals(1, repository.entries(ServiceInstanceBindingService.OBJECT_ID).size());

        Map<String, ServiceInstanceBinding> m = repository.entries(ServiceInstanceBindingService.OBJECT_ID);
        assertEquals(1, m.size());

        ServiceInstanceBinding sib2 = repository.get(ServiceInstanceBindingService.OBJECT_ID, sib.getId());
        assertNotNull(sib2);
        assertEquals("anID", sib2.getServiceInstanceId());

        ServiceInstanceBinding sib3 = repository.get(ServiceInstanceBindingService.OBJECT_ID, "98765");
        assertNotNull(sib3);
        assertEquals("anID", sib3.getServiceInstanceId());
        assertEquals("98765", sib3.getId());
        assertNotNull(sib3.getCredentials());

        // System.out.println(gson.toJson(sib3));

        repository.delete(ServiceInstanceBindingService.OBJECT_ID, sib3.getId());

        assertEquals(0, repository.entries(ServiceInstanceBindingService.OBJECT_ID).size());
    }
}