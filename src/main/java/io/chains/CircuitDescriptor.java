package io.chains;

import static java.util.Objects.requireNonNull;

import java.time.Duration;

/**
 * Holds the data a circuit can be constructed from. Used by {@link CircuitFactory}.
 */
public final class CircuitDescriptor {
    private ThrowingRunnable runnable;

    private int threshold;

    private Duration delay;

    /**
     * Returns a new {@code Builder} that can create an instance of this class wrapping the specified call.
     * @param runnable the call to be wrapped by the constructed descriptor
     * @return a new {@code Builder} that can be used to setup the descriptor
     */
    public static Builder wrapping(ThrowingRunnable runnable) {
        return new Builder(requireNonNull(runnable));
    }

    /**
     * Gets the wrapped runnable.
     * @return the wrapped runnable
     */
    public ThrowingRunnable getRunnable() {
        return runnable;
    }

    /**
     * Gets the threshold at which the circuit's state becomes <i>open</i>.
     * @return the threshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Gets the delay after the circuit retries the call even when in <i>open</i> state.
     * @return the delay
     */
    public Duration getDelay() {
        return delay;
    }

    /**
     * Builder class for {@code CircuitDescriptor} instances.
     */
    public static final class Builder {
        private ThrowingRunnable runnable;

        private int threshold;

        private Duration delay;

        private Builder(ThrowingRunnable runnable) {
            this.runnable = runnable;
        }

        /**
         * Sets the threshold.
         * @param threshold the threshold
         * @return the current {@code Builder} instance
         * @throws IllegalArgumentException if the threshold is less than or equal to zero
         */
        public Builder withThreshold(int threshold) {
            if (threshold <= 0) {
                throw new IllegalArgumentException("The threshold must be positive!");
            }

            this.threshold = threshold;

            return this;
        }

        /**
         * Sets the delay.
         * @param delay the delay
         * @return the current {@code Builder} instance
         * @throws NullPointerException if the argument is {@code null}
         */
        public Builder withDelay(Duration delay) {
            this.delay = requireNonNull(delay);

            return this;
        }

        /**
         * Returns the newly built {@code CircuitDescriptor} instance.
         * @return a new {@code CircuitDescriptor} instance
         * @throws IllegalStateException if the threshold is less than or equal to zero
         * @throws NullPointerException if the delay is unset
         */
        public CircuitDescriptor build() {
            checkFields();

            final CircuitDescriptor descriptor = new CircuitDescriptor();

            descriptor.delay = delay;
            descriptor.runnable = runnable;
            descriptor.threshold = threshold;

            return descriptor;
        }

        private void checkFields() {
            requireNonNull(delay);

            if (threshold == 0) {
                throw new IllegalStateException("The threshold must be positive!");
            }
        }
    }
}
