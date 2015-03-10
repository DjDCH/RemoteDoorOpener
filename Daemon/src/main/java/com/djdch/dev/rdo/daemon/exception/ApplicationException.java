package com.djdch.dev.rdo.daemon.exception;

public class ApplicationException extends Exception {

    private static final long serialVersionUID = 1040222395732495386L;

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
