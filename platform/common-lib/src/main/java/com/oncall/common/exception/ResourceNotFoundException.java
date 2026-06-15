package com.oncall.common.exception;

/**
 * Thrown when a requested resource cannot be found.
 * Maps to HTTP 404 via {@link GlobalExceptionHandler}.
 *
 * <p>SRP: this class has exactly one reason to change — the semantics of
 * "resource not found" in the domain.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
