/*
 Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.

 This program and the accompanying materials are made available under
 the terms of the under the Apache License, Version 2.0 (the "License”);
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package io.pivotal.ecosystem.service;

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
    private static final User.Role ROLE = User.Role.User;

    @Autowired
    private HelloController helloController;

    @Test
    public void testIt() {
        ResponseEntity<String> greeting = helloController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Sorry, I don't think we've met.", greeting.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, greeting.getStatusCode());

        ResponseEntity<User> in = helloController.createUser(new User(USER, ROLE));

        assertNotNull(in);
        assertEquals(HttpStatus.CREATED, in.getStatusCode());
        User u = in.getBody();
        assertNotNull(u);
        assertEquals(USER, u.getName());
        assertNotNull(u.getPassword());

        greeting = helloController.greeting(USER);
        assertNotNull(greeting);
        assertEquals("Hello, foo !", greeting.getBody());
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