package io.chains;

/**
 * Exception class wrapping another exception. Can be used to wrap checked exceptions and rethrow them.
 * This kind of exception is thrown if an async call wrapped by the circuit breaker fails.
 */
public class CheckedExceptionWrapperException extends RuntimeException {
    public CheckedExceptionWrapperException(Throwable cause) {
        super(cause);
    }
}
