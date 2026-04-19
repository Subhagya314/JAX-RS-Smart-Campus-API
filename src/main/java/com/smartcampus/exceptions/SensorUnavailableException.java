package com.smartcampus.exceptions;

/**
 * Custom exception thrown when attempting to add readings to a sensor that is not in an active state.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}