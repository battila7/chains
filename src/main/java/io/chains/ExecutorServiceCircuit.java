package io.chains;

import static io.chains.CircuitState.CLOSED;
import static io.chains.CircuitState.OPEN;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit class that uses an {@link ExecutorService} to dispatch async calls.
 */
final class ExecutorServiceCircuit implements Circuit, Shutdownable {
    private static final int DEFAULT_THREAD_POOL_SIZE = 3;

    private final CircuitDescriptor descriptor;

    /**
     * Counts the number of consecutive failures. Gets reset after a succeeding call.
     */
    private final AtomicInteger failCounter;

    private final AtomicReference<CircuitState> currentState;

    /**
     * The time the circuit tripped (set into <i>open</i> state).
     */
    private final AtomicReference<Instant> trippingFailure;

    private final ExecutorService executorService;

    ExecutorServiceCircuit(CircuitDescriptor descriptor) {
        this.descriptor = descriptor;
        this.failCounter = new AtomicInteger();
        this.currentState = new AtomicReference<>(CLOSED);
        this.trippingFailure = new AtomicReference<>();
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }

    @Override
    public CircuitDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public CircuitState getState() {
        return currentState.get();
    }

    @Override
    public Optional<Exception> executeSync() {
        if (!shouldPassCall()) {
            return Optional.of(new CircuitOpenException());
        }

        final Optional<Exception> result = executeWrappedSync();

        reconsiderState(result);

        return result;
    }

    @Override
    public CompletableFuture<Void> executeAsync() {
        if (!shouldPassCall()) {
            return CompletableFuture.runAsync(() -> { throw new CircuitOpenException(); }, executorService);
        }

        return CompletableFuture.runAsync(this::runWithExceptionWrapping, executorService)
                .exceptionally(throwable -> {
                    mayTrip();

                    throw new CheckedExceptionWrapperException(throwable.getCause().getCause());
                })
                .thenAccept(whoCares -> {
                    resetState();
                });
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Decides what to do with the failCounter and the currentState after a wrapped sync call.
     * @param result the result of the wrapped call
     */
    private void reconsiderState(Optional<Exception> result) {
        if (result.isPresent()) {
            mayTrip();
        } else {
            resetState();
        }
    }

    /**
     * Called after a wrapped call succeeds. Simply restarts the counter and closes the circuit.
     */
    private void resetState() {
        failCounter.set(0);

        currentState.set(CLOSED);
    }

    /**
     * Called after a wrapped call fails. Increments the failureCounter and if the threshold is reached, then <i>opens</i>
     * the circuit.
     */
    private void mayTrip() {
        final int failures = failCounter.incrementAndGet();

        if (failures >= descriptor.getThreshold()) {
            currentState.set(OPEN);
            trippingFailure.set(Instant.now());
        }
    }

    /**
     * Used for synchronous execution. Returns the exception thrown by the wrapped call.
     * @return an {@code Optional} with the exception thrown by the underlying call, or an
     *         empty {@code Optional} if there was no exception
     */
    private Optional<Exception> executeWrappedSync() {
        try {
            descriptor.getRunnable().run();

            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(e);
        }
    }

    /**
     * Used for asynchronous execution. Catches all exceptions thrown by the wrapped call and rethrows them
     * in a {@link CheckedExceptionWrapperException} which is then caught by the runtime.
     */
    private void runWithExceptionWrapping() {
        try {
            descriptor.getRunnable().run();
        } catch (Exception e) {
            throw new CheckedExceptionWrapperException(e);
        }
    }

    /**
     * Returns whether a request to call the underlying method should be passed or not. If the circuit is
     * <i>closed</i>, then the returned value is <i>true</i>. By default, when the circuit is <i>open</i>, the
     * returned value is <i>false</i>. However, if the given delay duration has passed, <i>true</i> is returned
     * disregarding the <i>open</i> state.
     * @return whether the call should be dispatched to the underlying runnable
     */
    private boolean shouldPassCall() {
        if (currentState.get() == OPEN) {
            final Duration sinceTripping = Duration.between(trippingFailure.get(), Instant.now());

            return (descriptor.getDelay().compareTo(sinceTripping) <= 0);
        }

        return true;
    }
}
