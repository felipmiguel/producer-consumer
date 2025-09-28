package com.example.producerconsumer;

import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * Main interface for the producer-consumer system that processes generic items.
 * Supports configurable number of producers and consumers.
 * @param <T> the type of items to be processed
 */
public interface ProducerConsumerService<T extends Item<?>> {
    
    /**
     * Starts the producer-consumer system with the specified configuration.
     * @param config the configuration specifying number of producers and consumers
     * @param producer the producer function that creates items
     * @param consumer the consumer function that processes items
     */
    void start(ProducerConsumerConfig config, Supplier<T> producer, Consumer<T> consumer);
    
    /**
     * Stops the producer-consumer system gracefully.
     */
    void stop();
    
    /**
     * Checks if the system is currently running.
     * @return true if the system is running, false otherwise
     */
    boolean isRunning();
    
    /**
     * Gets the current configuration of the system.
     * @return the current configuration
     */
    ProducerConsumerConfig getConfig();
    
    /**
     * Gets statistics about the current processing.
     * @return processing statistics
     */
    ProcessingStats getStats();
}