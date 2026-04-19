package com.smartcampus.exceptions;

/**
 * Custom exception thrown when a resource attempts to link to a parent/foreign resource that does not exist.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
