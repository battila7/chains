package io.chains;

/**
 * Interface for classes with shutdown logic.
 */
interface Shutdownable {
    /**
     * Releases the resources held by the instance.
     */
    void shutdown();
}
