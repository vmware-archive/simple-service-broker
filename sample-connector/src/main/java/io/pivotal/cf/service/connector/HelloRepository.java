package io.pivotal.cf.service.connector;


import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.web.bind.annotation.RequestBody;

public interface HelloRepository {

    @Headers("Content-Type: application/json")
    @RequestLine("POST /users")
    void createUser(@RequestBody User user);

    @Headers("Content-Type: application/json")
    @RequestLine("DELETE /users/{username}")
    void deleteUser(@Param(value = "username") String username);

    @Headers("Content-Type: application/json")
    @RequestLine("GET /greeting?username={username}")
    String greeting(@Param(value = "username") String username);
}