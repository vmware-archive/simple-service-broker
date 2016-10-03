package io.pivotal.cf.service.connector;


import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface HelloRepository {

    @Headers("Content-Type: application/json")
    @RequestLine("GET /login?username={username}&password={password}")
    void createUser(@Param(value = "username") String username, @Param(value = "password") String password);

    @Headers("Content-Type: application/json")
    @RequestLine("GET /logout?username={username}")
    void deleteUser(@Param(value = "username") String username);

    @Headers("Content-Type: application/json")
    @RequestLine("GET /greeting?username={username}")
    String greeting(@Param(value = "username") String username);
}