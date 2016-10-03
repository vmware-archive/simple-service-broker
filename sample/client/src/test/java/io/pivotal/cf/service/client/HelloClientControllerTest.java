package io.pivotal.cf.service.client;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class HelloClientControllerTest {

    private static final String USER = "foo" + System.currentTimeMillis();
    private static final String PW = "bar";

    @Autowired
    private HelloClientController helloClientController;

    @Test
    public void testIt() {
        try {
            helloClientController.greeting(USER);
        } catch (Exception e) {
            //expected
            assertTrue(e instanceof FeignException);
            assertEquals(401, ((FeignException) e).status());
        }

        String in = null;

        try {
            in = helloClientController.login(USER, PW);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(in);
        assertEquals("user: " + USER + " logged in.", in);

        String greeting = helloClientController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Hello, " + USER + "!", greeting);

        String out = helloClientController.logout(USER);
        assertNotNull(out);
        assertEquals("user: " + USER + " logged out.", out);

        try {
            helloClientController.greeting(USER);
        } catch (Exception e) {
            //expected
            assertTrue(e instanceof FeignException);
            assertEquals(401, ((FeignException) e).status());
        }
    }
}