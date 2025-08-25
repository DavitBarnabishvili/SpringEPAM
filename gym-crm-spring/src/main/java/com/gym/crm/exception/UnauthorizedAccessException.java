package com.gym.crm.exception;

public class UnauthorizedAccessException extends SecurityException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
