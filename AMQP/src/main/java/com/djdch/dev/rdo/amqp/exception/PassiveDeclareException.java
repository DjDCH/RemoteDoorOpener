package com.djdch.dev.rdo.amqp.exception;

public class PassiveDeclareException extends RuntimeException {

    public PassiveDeclareException(Exception e) {
        super(e);
    }
}
