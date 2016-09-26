package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
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
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Set;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class BindingServiceTest {

    @Autowired
    @InjectMocks
    BindingService bindingService;

    @Mock
    InstanceService instanceService;

    @Autowired
    private ServiceInstance serviceInstance;

    @Autowired
    private CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest;

    @Autowired
    private DeleteServiceInstanceBindingRequest deleteBindingRequest;

    @Resource(name = "bindingTemplate")
    private HashOperations<String, String, ServiceBinding> repo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(instanceService.getServiceInstance(Matchers.anyString())).thenReturn(serviceInstance);
        when(instanceService.saveInstance(any(ServiceInstance.class))).thenReturn(serviceInstance);
        when(instanceService.deleteInstance(any(ServiceInstance.class))).thenReturn(serviceInstance);

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
    public void testBinding() throws ServiceBrokerException {
        CreateServiceInstanceAppBindingResponse cresp = (CreateServiceInstanceAppBindingResponse)
                bindingService.createServiceInstanceBinding(createServiceInstanceBindingRequest);
        assertNotNull(cresp);
        assertNotNull(cresp.getCredentials());

        bindingService.deleteServiceInstanceBinding(deleteBindingRequest);
    }
}