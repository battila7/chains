package io.chains;

/**
 * Interface for runnables that can throw arbitrary exceptions.
 */
@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
