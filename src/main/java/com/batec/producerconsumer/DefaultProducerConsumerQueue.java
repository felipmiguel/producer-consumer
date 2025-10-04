package com.batec.producerconsumer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultProducerConsumerQueue<T> extends LinkedBlockingQueue<T> implements ProducerConsumerQueue<T> {

    public DefaultProducerConsumerQueue() {
        super();
    }

    public DefaultProducerConsumerQueue(int capacity) {
        super(capacity);
    }

    private final AtomicBoolean completed = new AtomicBoolean(false);

    @Override
    public boolean completed() {
        return completed.get() && this.isEmpty();
    }

    @Override
    public void complete() {
        this.completed.set(true);
    }

    @Override
    public void fail(Throwable t) {
        this.completed.set(true);
        throw new RuntimeException(t);
    }
}
