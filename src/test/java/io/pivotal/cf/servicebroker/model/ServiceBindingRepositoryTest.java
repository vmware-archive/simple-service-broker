package io.pivotal.cf.servicebroker.model;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.service.BindingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class ServiceBindingRepositoryTest {

    @Resource(name = "bindingTemplate")
    private HashOperations<String, String, ServiceBinding> repository;

    @Autowired
    private ServiceBinding serviceBinding;

    @Test
    public void instanceInsertedSuccessfully() throws Exception {
        repository.put(BindingService.OBJECT_ID, serviceBinding.getId(), serviceBinding);
        assertNotNull(repository.get(BindingService.OBJECT_ID, serviceBinding.getId()));

        repository.delete(BindingService.OBJECT_ID, serviceBinding.getId());
        assertNull(repository.get(BindingService.OBJECT_ID, serviceBinding.getId()));
    }
}