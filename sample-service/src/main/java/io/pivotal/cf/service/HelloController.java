package io.pivotal.cf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class HelloController {

    @Autowired
    private UserStore userStore;

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    ResponseEntity<User> createUser(@RequestBody User user) {
        userStore.addUser(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/users/{username}", method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteUser(@PathVariable(value = "username") String username) {
        userStore.deleteUser(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/greeting", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    ResponseEntity<String> greeting(@RequestParam(value = "username") String username) {
        String response = "Sorry, I don't think we've met.";
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        if (userStore.userExists(username)) {
            response = "Hello, " + username + "!";
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(response, status);
    }
}