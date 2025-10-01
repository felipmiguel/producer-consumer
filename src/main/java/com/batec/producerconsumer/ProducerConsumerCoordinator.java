package com.batec.producerconsumer;

import java.util.concurrent.ExecutorService;

/**
 * Coordinates the producer and consumer using a shared queue.
 *
 * @param <T> the type of items produced and consumed
 */
public class ProducerConsumerCoordinator<T> {

    private final Producer<T> producer;
    private final Consumer<T> consumer;
    private final ProducerConsumerQueue<T> queue;
    private final ExecutorService producerExecutor;
    private final ExecutorService consumerExecutor;

    private ProducerConsumerCoordinator(Builder<T> builder) {
        this.producer = builder.producer;
        this.consumer = builder.consumer;
        this.queue = builder.queue;
        this.bufferSize = builder.bufferSize;
        this.producerCount = builder.producerCount;
        this.consumerCount = builder.consumerCount;

        Thread producerThread = new Thread(() -> producer.produce(queue));
        Thread consumerThread = new Thread(() -> consumer.consume(queue));
        producerThread.start();
        consumerThread.start();
        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class Builder<T> {
        private Producer<T> producer;
        private Consumer<T> consumer;
        private ProducerConsumerQueue<T> queue;
        private int bufferSize = 10;
        private int producerCount = 1;
        private int consumerCount = 1;

        public Builder<T> producer(Producer<T> producer) {
            this.producer = producer;
            return this;
        }

        public Builder<T> consumer(Consumer<T> consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder<T> queue(ProducerConsumerQueue<T> queue) {
            this.queue = queue;
            return this;
        }

        public Builder<T> bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder<T> producerCount(int producerCount) {
            this.producerCount = producerCount;
            return this;
        }

        public Builder<T> consumerCount(int consumerCount) {
            this.consumerCount = consumerCount;
            return this;
        }

        public ProducerConsumerCoordinator<T> build() {
            if (producer == null || consumer == null) {
                throw new IllegalStateException("Producer, Consumer, and Queue must be set");
            }
            if (queue == null) {
                queue = new DefaultProducerConsumerQueue<>(bufferSize);
            }
            return new ProducerConsumerCoordinator<>(this);
        }
    }
}
