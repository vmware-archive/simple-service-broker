package io.pivotal.cf.service;

class HelloException extends RuntimeException {

    HelloException(String s) {
        super(s);
    }
}
