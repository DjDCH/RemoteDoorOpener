package com.djdch.dev.rdo.daemon.exception;

public class InvalidRequestException extends Exception {

    private static final long serialVersionUID = -7362171063958893322L;

    public InvalidRequestException(String message) {
        super(message);
    }
}
