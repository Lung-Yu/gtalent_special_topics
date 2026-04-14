package com.example.application.exception;

/**
 * Exception thrown when cursor decode fails or cursor format is invalid
 */
public class InvalidCursorException extends RuntimeException {
    
    /**
     * Constructor with message
     * @param message The error message
     */
    public InvalidCursorException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message The error message
     * @param cause The underlying cause
     */
    public InvalidCursorException(String message, Throwable cause) {
        super(message, cause);
    }
}
