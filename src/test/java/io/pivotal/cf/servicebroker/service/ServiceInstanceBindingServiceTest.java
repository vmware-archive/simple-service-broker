package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.persistance.ServiceInstance;
import io.pivotal.cf.servicebroker.persistance.ServiceInstanceBinding;
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
import org.springframework.data.redis.core.RedisTemplate;
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
public class ServiceInstanceBindingServiceTest {

    @Autowired
    @InjectMocks
    ServiceInstanceBindingService serviceInstanceBindingService;

    @Mock
    ServiceInstanceService serviceInstanceService;

    @Resource(name = "sibTemplate")
    private HashOperations<String, String, ServiceInstanceBinding> repo;

    @Autowired
    @Resource(name = "sibTemplate")
    private RedisTemplate<String, ServiceInstanceBinding> template;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ServiceInstance si = TestConfig.getServiceInstance();

        when(serviceInstanceService.getServiceInstance(Matchers.anyString()))
                .thenReturn(si);

        when(
                serviceInstanceService
                        .saveInstance(any(ServiceInstance.class)))
                .thenReturn(si);

        when(
                serviceInstanceService
                        .deleteInstance(any(ServiceInstance.class)))
                .thenReturn(si);

        Set<String> keys = repo.keys(ServiceInstanceBindingService.OBJECT_ID);
        for (String key : keys) {
            repo.delete(ServiceInstanceBindingService.OBJECT_ID, key);
        }
    }

    @After
    public void cleanUp() throws Exception {
        Set<String> keys = repo.keys(ServiceInstanceBindingService.OBJECT_ID);
        for (String key : keys) {
            repo.delete(ServiceInstanceBindingService.OBJECT_ID, key);
        }
    }

    @Test
    public void testBinding() throws ServiceBrokerException,
            ServiceInstanceBindingExistsException {

        ServiceInstanceBinding b = TestConfig.getServiceInstanceBinding();
        assertNotNull(b);
        Map<String, Object> m = b.getCredentials();
        assertNotNull(m);
        assertNotNull(b.getId());
        assertEquals("anID", b.getServiceInstanceId());
    }
}
