package io.pivotal.cf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

    @Autowired
    private UserStore userStore;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    ResponseEntity<Void> login(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {
        userStore.addUser(username, password);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    ResponseEntity<Void> logout(@RequestParam(value = "username") String username) {
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