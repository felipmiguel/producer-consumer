package com.batec.producerconsumer;

import java.util.concurrent.BlockingQueue;

public interface ProducerConsumerQueue<T> extends BlockingQueue<T>, ConsumerQueue<T>, ProducerQueue<T> {
}
