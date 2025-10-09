package com.batec.producerconsumer;

public interface ProducerQueue<T>  {

    void put(T item) throws InterruptedException;

    void complete();

    void fail(Throwable t);
}
