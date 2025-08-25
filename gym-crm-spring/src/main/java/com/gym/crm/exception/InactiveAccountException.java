package com.gym.crm.exception;

public class InactiveAccountException extends SecurityException {
    public InactiveAccountException(String message) {
        super(message);
    }

    public InactiveAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}