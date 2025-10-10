package com.batec.producerconsumer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public class WorkloadConfiguration<T> {
    private final Consumer<ProducerQueue<T>> producer;
    private final Consumer<ConsumerQueue<T>> queueConsumer;
    private final Consumer<T> itemConsumer;
    private final int bufferSize;
    private final int producerCount;
    private final int consumerCount;
    private final Duration producerTerminationTimeout;
    private final Duration consumerTerminationTimeout;

    public Consumer<ProducerQueue<T>> getProducer() {
        return producer;
    }

    public Consumer<ConsumerQueue<T>> getQueueConsumer() {
        return queueConsumer;
    }

    public Consumer<T> getItemConsumer() {
        return itemConsumer;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getProducerCount() {
        return producerCount;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public Duration getProducerTerminationTimeout() {
        return producerTerminationTimeout;
    }

    public Duration getConsumerTerminationTimeout() {
        return consumerTerminationTimeout;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private WorkloadConfiguration(Builder<T> builder) {
        this.producer = builder.producer;
        this.queueConsumer = builder.queueConsumer;
        this.itemConsumer = builder.itemConsumer;
        this.bufferSize = builder.bufferSize;
        this.producerCount = builder.producerCount;
        this.consumerCount = builder.consumerCount;
        this.producerTerminationTimeout = builder.producerTerminationTimeout;
        this.consumerTerminationTimeout = builder.consumerTerminationTimeout;
    }


    // Builder pattern implementation
    public static class Builder<T> {
        private Consumer<ProducerQueue<T>> producer;
        private Consumer<ConsumerQueue<T>> queueConsumer;
        private Consumer<T> itemConsumer;
        private int bufferSize = 1;
        private int producerCount = 1;
        private int consumerCount = 1;
        private Duration producerTerminationTimeout = Duration.of(1, ChronoUnit.SECONDS);
        private Duration consumerTerminationTimeout = Duration.of(1, ChronoUnit.SECONDS);

        public Builder<T> producer(Consumer<ProducerQueue<T>> producer) {
            this.producer = producer;
            return this;
        }

        public Builder<T> queueConsumer(Consumer<ConsumerQueue<T>> queueConsumer) {
            this.queueConsumer = queueConsumer;
            return this;
        }

        public Builder<T> itemConsumer(Consumer<T> itemConsumer) {
            this.itemConsumer = itemConsumer;
            return this;
        }

        public Builder<T> bufferSize(int bufferSize) {
            if (bufferSize <= 0) {
                throw new IllegalArgumentException("Buffer size must be greater than 0");
            }
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder<T> producerCount(int producerCount) {
            if (producerCount <= 0) {
                throw new IllegalArgumentException("Producer count must be greater than 0");
            }
            this.producerCount = producerCount;
            return this;
        }

        public Builder<T> consumerCount(int consumerCount) {
            if (consumerCount <= 0) {
                throw new IllegalArgumentException("Consumer count must be greater than 0");
            }
            this.consumerCount = consumerCount;
            return this;
        }

        public Builder<T> producerTerminationTimeout(Duration timeout) {
            this.producerTerminationTimeout = timeout;
            return this;
        }

        public Builder<T> consumerTerminationTimeout(Duration timeout) {
            this.consumerTerminationTimeout = timeout;
            return this;
        }

        public WorkloadConfiguration<T> build() {
            if (producer == null) {
                throw new IllegalArgumentException("Producer function must be provided");
            }
            if (queueConsumer == null && itemConsumer == null) {
                throw new IllegalArgumentException("Either queueConsumer or itemConsumer must be provided");
            }
            if (queueConsumer != null && itemConsumer != null) {
                throw new IllegalArgumentException("Only one of queueConsumer or itemConsumer should be provided");
            }
            return new WorkloadConfiguration<>(this);
        }
    }

}
