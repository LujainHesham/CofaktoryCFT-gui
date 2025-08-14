package com.cofaktory.footprint.myExceptions;

public class AuthenticationException extends Exception {
    // Basic constructor
    public AuthenticationException(String message) {
        super(message);
    }

    // Constructor with cause
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}