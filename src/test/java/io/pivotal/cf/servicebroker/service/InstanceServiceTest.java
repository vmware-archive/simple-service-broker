package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Set;

import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class InstanceServiceTest {

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private CreateServiceInstanceRequest createServiceInstanceRequest;

    @Autowired
    private UpdateServiceInstanceRequest updateServiceInstanceRequest;

    @Autowired
    private DeleteServiceInstanceRequest deleteServiceInstanceRequest;

    @Resource(name = "instanceTemplate")
    private HashOperations<String, String, ServiceInstance> repo;

    @Before
    public void setUp() throws Exception {
        Set<String> keys = repo.keys(InstanceService.OBJECT_ID);
        for (String key : keys) {
            repo.delete(InstanceService.OBJECT_ID, key);
        }
    }

    @After
    public void cleanUp() throws Exception {
        Set<String> keys = repo.keys(InstanceService.OBJECT_ID);
        for (String key : keys) {
            repo.delete(InstanceService.OBJECT_ID, key);
        }
    }

    @Test
    public void testInstance() throws ServiceBrokerException {
        assertNotNull(instanceService.createServiceInstance(createServiceInstanceRequest));
        assertNotNull(instanceService.updateServiceInstance(updateServiceInstanceRequest));
        assertNotNull(instanceService.deleteServiceInstance(deleteServiceInstanceRequest));
    }
}