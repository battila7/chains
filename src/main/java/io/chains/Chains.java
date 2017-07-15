package io.chains;

import java.util.WeakHashMap;

/**
 * Class responsible for starting up and shutting down the library.
 */
public final class Chains {
    private static final Object MAP_VALUE = new Object();

    private static final WeakHashMap<Shutdownable, Object> shutdownableCircuits = new WeakHashMap<>();

    private Chains() {
        /*
         * Cannot be constructed.
         */
    }

    /**
     * Shuts down and closes the resources (for example thread pools) managed by the library.
     */
    public static void shutdown() {
        shutdownableCircuits.keySet()
                .forEach(Shutdownable::shutdown);
    }

    /**
     * Adds a new shutdownable object to the list (map) of managed objects. Objects registered this way will be
     * shut down when the {@link Chains#shutdown()} method is called.
     * @param shutdownable the object to be managed
     */
    static void registerShutdownable(Shutdownable shutdownable) {
        shutdownableCircuits.put(shutdownable, MAP_VALUE);
    }
}
