package io.pivotal.cf.service.client;

import io.pivotal.cf.service.connector.HelloRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloClientController {

    public HelloClientController(HelloRepository helloRepository) {
        super();
        this.helloRepository = helloRepository;
    }

    private HelloRepository helloRepository;

    @RequestMapping(value = "/greeting", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    String greeting(@RequestParam(value = "username") String username) {
        return helloRepository.greeting(username);
    }
}