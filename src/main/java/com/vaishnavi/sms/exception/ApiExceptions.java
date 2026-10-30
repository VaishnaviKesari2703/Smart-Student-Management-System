package com.vaishnavi.sms.exception;

import java.util.Collections;
import java.util.Map;

/** Container for the application's domain exceptions. */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** Requested record does not exist (HTTP 404). */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    /** Record would duplicate a unique value (HTTP 409). */
    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) {
            super(message);
        }
    }

    /** Operation violates a business rule such as attendance bounds (HTTP 400). */
    public static class InvalidOperationException extends RuntimeException {
        public InvalidOperationException(String message) {
            super(message);
        }
    }

    /** Input failed validation (HTTP 400) with per-field messages. */
    public static class ValidationException extends RuntimeException {
        private final Map<String, String> fieldErrors;

        public ValidationException(Map<String, String> fieldErrors) {
            super("Validation failed: " + String.join("; ", fieldErrors.values()));
            this.fieldErrors = Collections.unmodifiableMap(fieldErrors);
        }

        public Map<String, String> getFieldErrors() {
            return fieldErrors;
        }
    }
}
