package io.chains;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for a circuit that wraps a specific call. The call wrapped by the circuit
 * can be executed either synchronously or asynchronously.
 */
public interface Circuit {
    /**
     * Gets the descriptor this circuit was created from.
     * @return the descriptor
     */
    CircuitDescriptor getDescriptor();

    /**
     * Gets the current state of the circuit.
     * @return the state of the circuit
     */
    CircuitState getState();

    /**
     * Executes the wrapped call synchronously. If the circuit is <i>open</i>, then the returned {@code Optional}
     * contains an instance of {@link CircuitOpenException}.
     * @return an {@code Optional} holding the {@code Exception} thrown by the underlying call, or
     *         an empty {@code Optional} if there was no {@code Exception}
     */
    Optional<Exception> executeSync();

    /**
     * Executes the wrapped call asynchronously. If the circuit is <i>open</i>, then the returned future completes
     * exceptionally with an instance of {@link CircuitOpenException}. If the circuit is closed, but an exception occurs,
     * then the original exception is accessible inside an {@link CheckedExceptionWrapperException}.
     * @return a future that succeeds if the underlying call succeeds, otherwise completes exceptionally
     */
    CompletableFuture<Void> executeAsync();
}
