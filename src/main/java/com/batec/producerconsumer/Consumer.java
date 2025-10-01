package com.batec.producerconsumer;

public interface Consumer<T> {

    void consume(ConsumerQueue<T> queue);
}
