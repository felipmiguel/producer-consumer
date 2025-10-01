package com.batec.producerconsumer;

import java.util.concurrent.LinkedBlockingQueue;

public class DefaultProducerConsumerQueue<T> extends LinkedBlockingQueue<T> implements ProducerConsumerQueue<T> {

    public DefaultProducerConsumerQueue() {
        super();
    }

    public DefaultProducerConsumerQueue(int capacity) {
        super(capacity);
    }

    private volatile boolean completed = false;

    @Override
    public boolean completed() {
        return completed && this.isEmpty();
    }

    @Override
    public void complete() {
        this.completed = true;
    }

    @Override
    public void fail(Throwable t) {
        this.completed = true;
        throw new RuntimeException(t);
    }
}
