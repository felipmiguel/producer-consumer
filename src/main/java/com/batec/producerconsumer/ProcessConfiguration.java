package com.batec.producerconsumer;

import java.util.function.Consumer;

public class ProcessConfiguration<T> {
    private Consumer<ProducerQueue<T>> producer;
    private Consumer<ConsumerQueue<T>> consumer;
    private int bufferSize;
    private int producerCount;
    private int consumerCount;


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
}
