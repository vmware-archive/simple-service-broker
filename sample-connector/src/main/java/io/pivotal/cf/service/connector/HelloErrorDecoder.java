package io.pivotal.cf.service.connector;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.io.IOException;

class HelloErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String content = "Error accessing hello-service.";
        try {
            content = Util.toString(response.body().asReader());
        } catch (IOException e) {
            //no content? ignore.
        }
        return new HelloException(content, HttpStatus.valueOf(response.status()));
    }
}