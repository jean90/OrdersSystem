package com.amazingco.core.exception;

/**
 * Base type for domain/business-rule violations, as opposed to infrastructure failures.
 * Never thrown directly — every service-specific exception extends this.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
