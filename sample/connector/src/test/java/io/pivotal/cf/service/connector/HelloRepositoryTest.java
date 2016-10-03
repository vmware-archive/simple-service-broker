package io.pivotal.cf.service.connector;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class, loader = AnnotationConfigContextLoader.class)
public class HelloRepositoryTest {

    private static final String USER = "foo";
    private static final String PW = "bar";

    @Autowired
    private HelloRepository helloRepository;

    @Test
    public void testIt() {
        try {
            helloRepository.greeting(USER);
        } catch (Exception e) {
            //expected
            assertTrue(e instanceof FeignException);
            assertEquals(401, ((FeignException) e).status());
        }

        try {
            helloRepository.createUser(USER, PW);
        } catch (Exception e) {
            fail("creds should have been accepted.");
        }

        String greeting = helloRepository.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Hello, foo!", greeting);

        helloRepository.deleteUser(USER);

        try {
            helloRepository.greeting(USER);
        } catch (Exception e) {
            //expected
            assertTrue(e instanceof FeignException);
            assertEquals(401, ((FeignException) e).status());
        }
    }
}