package com.batec.producerconsumer;

public interface Producer <T> {

    void produce(ProducerQueue<T> queue);
}
