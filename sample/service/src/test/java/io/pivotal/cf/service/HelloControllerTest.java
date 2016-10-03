package io.pivotal.cf.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class HelloControllerTest {

    private static final String USER = "foo";
    private static final String PW = "bar";


    @Autowired
    private HelloController helloController;

    @Test
    public void testIt() {
        ResponseEntity<String> greeting = helloController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Sorry, I don't think we've met.", greeting.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, greeting.getStatusCode());

        ResponseEntity<Void> in = helloController.login(USER, PW);

        assertNotNull(in);
        assertEquals(HttpStatus.CREATED, in.getStatusCode());

        greeting = helloController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Hello, foo!", greeting.getBody());
        assertEquals(HttpStatus.OK, greeting.getStatusCode());

        ResponseEntity<Void> out = helloController.logout(USER);
        assertNotNull(out);
        assertEquals(HttpStatus.OK, out.getStatusCode());

        greeting = helloController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Sorry, I don't think we've met.", greeting.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, greeting.getStatusCode());
    }
}