package io.pivotal.cf.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloControllerTest {

    private static final String USER = "foo";
    private static final String PW = "bar";
    private static final User.UserType TYPE = User.UserType.User;

    @Autowired
    private HelloController helloController;

    @Test
    public void testIt() {
        ResponseEntity<String> greeting = helloController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Sorry, I don't think we've met.", greeting.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, greeting.getStatusCode());

        ResponseEntity<User> in = helloController.createUser(new User(USER, PW, TYPE));

        assertNotNull(in);
        assertEquals(HttpStatus.CREATED, in.getStatusCode());
        User u = in.getBody();
        assertNotNull(u);
        assertEquals(USER, u.getName());
        assertEquals(PW, u.getPassword());

        greeting = helloController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Hello, foo!", greeting.getBody());
        assertEquals(HttpStatus.OK, greeting.getStatusCode());

        ResponseEntity<Void> out = helloController.deleteUser(USER);
        assertNotNull(out);
        assertEquals(HttpStatus.OK, out.getStatusCode());

        greeting = helloController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Sorry, I don't think we've met.", greeting.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, greeting.getStatusCode());
    }
}