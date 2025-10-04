package com.batec.producerconsumer;

import java.util.function.Consumer;

public class ProcessConfiguration<T> {
    private Consumer<ProducerQueue<T>> producer;
    private Consumer<ConsumerQueue<T>> consumer;
    private int bufferSize;
    private int producerCount;
    private int consumerCount;
    private long producerTerminationTimeout = 1; // default 1 second
    private long consumerTerminationTimeout = 1; // default 1 second


    public Consumer<ProducerQueue<T>> getProducer() {
        return producer;
    }

    public void setProducer(Consumer<ProducerQueue<T>> producer) {
        this.producer = producer;
    }

    public Consumer<ConsumerQueue<T>> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<ConsumerQueue<T>> consumer) {
        this.consumer = consumer;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getProducerCount() {
        return producerCount;
    }

    public void setProducerCount(int producerCount) {
        this.producerCount = producerCount;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(int consumerCount) {
        this.consumerCount = consumerCount;
    }

    public long getProducerTerminationTimeout() {
        return producerTerminationTimeout;
    }

    public void setProducerTerminationTimeout(long producerTerminationTimeout) {
        this.producerTerminationTimeout = producerTerminationTimeout;
    }

    public long getConsumerTerminationTimeout() {
        return consumerTerminationTimeout;
    }

    public void setConsumerTerminationTimeout(long consumerTerminationTimeout) {
        this.consumerTerminationTimeout = consumerTerminationTimeout;
    }
}
