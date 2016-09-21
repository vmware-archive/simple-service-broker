package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class BindingServiceTest {

    @Autowired
    @InjectMocks
    BindingService serviceInstanceBindingService;

    @Mock
    InstanceService instanceService;

    @Resource(name = "bindingTemplate")
    private HashOperations<String, String, ServiceBinding> repo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ServiceInstance si = TestConfig.getServiceInstance();

        when(instanceService.getServiceInstance(Matchers.anyString())).thenReturn(si);
        when(instanceService.saveInstance(any(ServiceInstance.class))).thenReturn(si);

        when(instanceService.deleteInstance(any(ServiceInstance.class))).thenReturn(si);

        Set<String> keys = repo.keys(BindingService.OBJECT_ID);
        for (String key : keys) {
            repo.delete(BindingService.OBJECT_ID, key);
        }
    }

    @After
    public void cleanUp() throws Exception {
        Set<String> keys = repo.keys(BindingService.OBJECT_ID);
        for (String key : keys) {
            repo.delete(BindingService.OBJECT_ID, key);
        }
    }

    @Test
    public void testBinding() throws ServiceBrokerException,
            ServiceInstanceBindingExistsException {

        ServiceBinding b = TestConfig.getServiceInstanceBinding();
        assertNotNull(b);
        assertNotNull(b.getId());
        assertEquals("12345", b.getId());
    }

    //TODO fix these tests, plus add is tests too
}