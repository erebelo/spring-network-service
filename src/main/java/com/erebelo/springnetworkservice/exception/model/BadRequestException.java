package com.erebelo.springnetworkservice.exception.model;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
