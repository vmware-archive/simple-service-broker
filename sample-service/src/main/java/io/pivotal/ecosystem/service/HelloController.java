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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
class HelloController {

    private UserStore userStore;

    public HelloController(UserStore userStore) {
        super();
        this.userStore = userStore;
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    ResponseEntity<User> createUser(@RequestBody User user) {
        if (userStore.userExists(user.getName())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        setPassword(user);
        userStore.save(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    ResponseEntity<User> updateUser(@RequestBody User user) {
        if (!userStore.userExists(user.getName())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        setPassword(user);
        userStore.save(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/users/{username}", method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteUser(@PathVariable(value = "username") String username) {
        if (!userStore.userExists(username)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userStore.delete(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/greeting", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    ResponseEntity<String> greeting(@RequestParam(value = "username") String username) {
        String response = "Sorry, I don't think we've met.";
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        if (userStore.userExists(username)) {
            response = "Hello, " + username + " !";
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(response, status);
    }

    private void setPassword(User user) {
        String pw = UUID.randomUUID().toString();
        user.setPassword(pw);
    }
}