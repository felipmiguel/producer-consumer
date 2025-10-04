package com.batec.producerconsumer;

import java.util.concurrent.TimeUnit;

public interface ConsumerQueue<T> {
    boolean completed();
    T take() throws InterruptedException;
    T poll(long timeout, TimeUnit unit) throws InterruptedException;
}
