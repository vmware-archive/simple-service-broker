package io.pivotal.cf.service.connector;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class HelloException extends RuntimeException {

    private HttpStatus status;

    HelloException(String s, HttpStatus status) {
        super(s);
        this.status = status;
    }
}