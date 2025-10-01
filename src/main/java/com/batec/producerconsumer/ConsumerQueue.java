package com.batec.producerconsumer;

public interface ConsumerQueue<T> {
    boolean completed();
    T take() throws InterruptedException;
}
