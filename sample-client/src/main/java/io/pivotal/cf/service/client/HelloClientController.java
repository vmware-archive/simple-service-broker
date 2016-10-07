package io.pivotal.cf.service.client;

import io.pivotal.cf.service.connector.HelloException;
import io.pivotal.cf.service.connector.HelloRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloClientController {

    public HelloClientController(HelloRepository helloRepository) {
        this.helloRepository = helloRepository;
    }

    private HelloRepository helloRepository;

    @RequestMapping(value = "/greeting", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    ResponseEntity<String> greeting(@RequestParam(value = "username") String username) {
        try {
            return new ResponseEntity<>(helloRepository.greeting(username), HttpStatus.OK);
        } catch (HelloException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }
}