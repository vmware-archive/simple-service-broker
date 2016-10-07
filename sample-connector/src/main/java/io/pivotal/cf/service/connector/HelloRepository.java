package io.pivotal.cf.service.connector;


import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface HelloRepository {

    @Headers("Content-Type: application/json")
    @RequestLine("GET /greeting?username={username}")
    String greeting(@Param(value = "username") String username);
}