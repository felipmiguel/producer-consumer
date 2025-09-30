package com.example.producerconsumer;

/**
 * Configuration class for the producer-consumer system.
 * Allows configuring the number of producers and consumers.
 */
public class ProducerConsumerConfig {
    private final int numberOfProducers;
    private final int numberOfConsumers;
    private final int queueCapacity;

    /**
     * Creates a new configuration with the specified parameters.
     *
     * @param numberOfProducers the number of producer threads
     * @param numberOfConsumers the number of consumer threads
     * @param queueCapacity     the maximum capacity of the internal queue
     */
    public ProducerConsumerConfig(int numberOfProducers, int numberOfConsumers, int queueCapacity) {
        if (numberOfProducers <= 0) {
            throw new IllegalArgumentException("Number of producers must be positive");
        }
        if (numberOfConsumers <= 0) {
            throw new IllegalArgumentException("Number of consumers must be positive");
        }
        if (queueCapacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be positive");
        }

        this.numberOfProducers = numberOfProducers;
        this.numberOfConsumers = numberOfConsumers;
        this.queueCapacity = queueCapacity;
    }

    /**
     * Creates a default configuration with 1 producer, 1 consumer, and reasonable defaults.
     */
    public ProducerConsumerConfig() {
        this(1, 1, 100);
    }

    public int getNumberOfProducers() {
        return numberOfProducers;
    }

    public int getNumberOfConsumers() {
        return numberOfConsumers;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    @Override
    public String toString() {
        return String.format("ProducerConsumerConfig{producers=%d, consumers=%d, queueCapacity=%d}",
                numberOfProducers, numberOfConsumers, queueCapacity);
    }
}