package io.chains;

/**
 * Exception class thrown/returned when the wrapped call fails because the circuit is in <i>open</i> state.
 */
public class CircuitOpenException extends RuntimeException {
    private static final String MESSAGE = "The call failed, because the circuit is open.";

    public CircuitOpenException() {
        super(MESSAGE);
    }
}
