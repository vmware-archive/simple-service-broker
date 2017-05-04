package io.pivotal.ecosystem.servicebroker.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.validation.constraints.Null;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CasheRepoTest {


    @InjectMocks
    private CasheRepo casheRepo = new CasheRepo();

    private @Mock
    RedisConnection redisConnectionMock;
    private @Mock

    RedisConnectionFactory redisConnectionFactoryMock;

    @Spy
    private RedisTemplate redisTemplate;

    @Before
    public void setUp() {
        when(redisConnectionFactoryMock.getConnection()).thenReturn(redisConnectionMock);
        redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactoryMock);
        redisTemplate.afterPropertiesSet();
    }

    @Test
    public void getObjectTest() {
        Object e;
        doThrow(NullPointerException).when(redisTemplate.opsForValue()).set("spring", "data");
        redisTemplate.afterPropertiesSet();
        System.out.println(redisTemplate.opsForValue().get("spring"));
    }


}
