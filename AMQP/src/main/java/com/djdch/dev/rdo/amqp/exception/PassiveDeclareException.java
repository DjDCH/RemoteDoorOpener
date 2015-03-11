package com.djdch.dev.rdo.amqp.exception;

public class PassiveDeclareException extends RuntimeException {

    private static final long serialVersionUID = -2921599750107191701L;

    public PassiveDeclareException(Exception e) {
        super(e);
    }
}
