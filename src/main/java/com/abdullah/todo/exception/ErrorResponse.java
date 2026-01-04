package com.abdullah.todo.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response DTO for API errors.
 * 
 * Provides a consistent error format across all endpoints:
 * - timestamp: When the error occurred
 * - status: HTTP status code
 * - error: HTTP status reason phrase
 * - message: Human-readable error description
 * - path: The request path that caused the error
 * - errors: List of validation errors (for 400 Bad Request)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    /**
     * List of field-level validation errors.
     * Only populated for validation failures.
     */
    private List<FieldError> errors;

    /**
     * Inner class representing a single field validation error.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
