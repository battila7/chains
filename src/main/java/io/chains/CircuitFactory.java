package io.chains;

import static java.util.Objects.requireNonNull;

/**
 * Factory class responsible for creating {@link Circuit} instances.
 */
public final class CircuitFactory {
    private CircuitFactory() {
        /*
         * Cannot be constructed.
         */
    }

    /**
     * Constructs a new {@code Circuit} instance from the specified descriptor.
     * @param descriptor the descriptor containing necessary attributes
     * @return a new {@code Circuit} instance
     * @throws NullPointerException if the descriptor is {@code null}
     */
    public static Circuit fromDescriptor(CircuitDescriptor descriptor) {
        final ExecutorServiceCircuit circuit = new ExecutorServiceCircuit(requireNonNull(descriptor));

        Chains.registerShutdownable(circuit);

        return circuit;
    }
}
