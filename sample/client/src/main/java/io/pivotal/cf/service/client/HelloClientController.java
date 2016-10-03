package io.pivotal.cf.service.client;

import io.pivotal.cf.service.connector.HelloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloClientController {

    @Autowired
    private HelloRepository repo;

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    String login(@RequestParam(value = "username") String username, @RequestParam(value = "password", required = true) String password) throws Exception {
        try {
            repo.createUser(username, password);
        }
        catch (Exception e) {
            throw new HelloClientException(e.getMessage());
        }
        return "user: " + username + " logged in.";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    String logout(@RequestParam(value = "username") String username) {
        repo.deleteUser(username);
        return "user: " + username + " logged out.";
    }

    @RequestMapping(value = "/greeting", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    String greeting(@RequestParam(value = "username") String username) {
        return repo.greeting(username);
    }
}