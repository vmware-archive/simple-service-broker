package io.pivotal.cf.service.connector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class, loader = AnnotationConfigContextLoader.class)
public class HelloRepositoryTest {

    private static final String NAME = "foo" + System.currentTimeMillis();

    private static final User USER = new User(NAME, User.Role.User);

    @MockBean
    private HelloRepository helloRepository;

    @Before
    public void setup() {
        Answer<Void> a = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        };

        doAnswer(a).when(helloRepository).createUser(USER);

        given(this.helloRepository.greeting(NAME)).willReturn("Hello, " + USER.getName() + "!");
    }

    @Test
    public void testIt() {
        helloRepository.createUser(USER);
        String greeting = helloRepository.greeting(USER.getName());
        assertNotNull(greeting);
        assertEquals("Hello, " + USER.getName() + "!", greeting);

        helloRepository.deleteUser(USER.getName());
    }
}