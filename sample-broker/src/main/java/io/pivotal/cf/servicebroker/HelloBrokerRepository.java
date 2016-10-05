package io.pivotal.cf.servicebroker;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.pivotal.cf.service.connector.User;
import org.springframework.web.bind.annotation.RequestBody;

interface HelloBrokerRepository {

    //TODO how to test this with mocks?

    //TODO create types of users: admin for CI and user for BI, plus fix these!
    @Headers("Content-Type: application/json")
    @RequestLine("POST /users")
    User provisionUser(@RequestBody User user);

    @Headers("Content-Type: application/json")
    @RequestLine("DELETE /users/{username}")
    void deprovisionUser(@Param(value = "username") String username);

    @Headers("Content-Type: application/json")
    @RequestLine("PUT /users/{username}")
    User updateUser(@Param(value = "username") String username, @RequestBody User user);
}