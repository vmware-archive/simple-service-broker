package io.pivotal.cf.service;

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

    //TODO protect with basic auth (admin)
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    ResponseEntity<User> createUser(@RequestBody User user) {
        if (userStore.userExists(user.getName())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        setPassword(user);
        userStore.save(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    //TODO protect with basic auth (admin)
    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    ResponseEntity<User> updateUser(@RequestBody User user) {
        if (!userStore.userExists(user.getName())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        setPassword(user);
        userStore.save(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    //TODO protect with basic auth (admin)
    @RequestMapping(value = "/users/{username}", method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteUser(@PathVariable(value = "username") String username) {
        if (!userStore.userExists(username)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userStore.delete(username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //TODO protect with basic auth (user)
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