package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.TestConfig;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceServiceTest {

    @Autowired
    @InjectMocks
    private InstanceService instanceService;

    @Autowired
    private ServiceInstance serviceInstance;

    @Autowired
    private CreateServiceInstanceRequest createServiceInstanceRequest;

    @Autowired
    private UpdateServiceInstanceRequest updateServiceInstanceRequest;

    @Autowired
    private DeleteServiceInstanceRequest deleteServiceInstanceRequest;

    @Mock
    private HashOperations<String, String, ServiceInstance> repo;

    private final Map<String, ServiceInstance> fakeRepo = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInstance() throws ServiceBrokerException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                fakeRepo.put(serviceInstance.getId(), serviceInstance);
                return null;
            }
        }).when(repo).put(anyString(), anyString(), any(ServiceInstance.class));

        assertNotNull(instanceService.createServiceInstance(createServiceInstanceRequest));

        when(repo.get(Matchers.anyString(), Matchers.anyString())).thenReturn(fakeRepo.get(TestConfig.SI_ID));

        assertNotNull(instanceService.updateServiceInstance(updateServiceInstanceRequest));
        assertNotNull(instanceService.deleteServiceInstance(deleteServiceInstanceRequest));
    }
}