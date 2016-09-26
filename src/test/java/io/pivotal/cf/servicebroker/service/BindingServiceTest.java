package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
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
    private ServiceBinding serviceBinding;

    @Autowired
    private CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest;

    @Autowired
    private DeleteServiceInstanceBindingRequest deleteBindingRequest;

    private final Map<String, ServiceBinding> fakeRepo = new HashMap<>();

    @Mock
    private HashOperations<String, String, ServiceBinding> repo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(instanceService.getServiceInstance(anyString())).thenReturn(serviceInstance);
    }

    @Test
    public void testBinding() throws ServiceBrokerException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                fakeRepo.put(serviceBinding.getId(), serviceBinding);
                return null;
            }
        }).when(repo).put(anyString(), anyString(), any(ServiceBinding.class));

        CreateServiceInstanceAppBindingResponse cresp = (CreateServiceInstanceAppBindingResponse)
                bindingService.createServiceInstanceBinding(createServiceInstanceBindingRequest);
        assertNotNull(cresp);
        assertNotNull(cresp.getCredentials());

        when(repo.get(Matchers.anyString(), Matchers.anyString())).thenReturn(fakeRepo.get(TestConfig.SB_ID));

        bindingService.deleteServiceInstanceBinding(deleteBindingRequest);
    }
}