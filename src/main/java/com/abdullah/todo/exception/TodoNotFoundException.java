package com.abdullah.todo.exception;

/**
 * Custom exception thrown when a requested Todo is not found.
 * 
 * Using a custom exception instead of returning null provides:
 * - Clear intent (the name explains the problem)
 * - Centralized handling via @ControllerAdvice
 * - Clean API responses with appropriate HTTP status codes
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(Long id) {
        super(String.format("Todo not found with id: %d", id));
    }

    public TodoNotFoundException(String message) {
        super(message);
    }
}
