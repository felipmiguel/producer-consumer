package com.batec.producerconsumer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicTests {

    @Test
    void testProducerConsumer() {
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        ProcessConfiguration<Integer> config = new ProcessConfiguration<>();
        config.setBufferSize(10);
        config.setProducerCount(1);
        config.setConsumerCount(10);
        config.setProducer(producerQueue -> {
            for (int i = 0; i < 100; i++) {
                try {
                    producerQueue.put(i);
                    producedCount.incrementAndGet();
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            producerQueue.complete();
        });
        config.setConsumer(consumerQueue -> {
            while (!consumerQueue.completed()) {
                try {
                    Integer item = consumerQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (item != null) {
                        consumedCount.incrementAndGet();
                        Thread.sleep(8);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        ProducerConsumerCoordinator.doWork(config).thenAccept(nothing -> {
            assertThat(producedCount.get()).isEqualTo(consumedCount.get());
            completed.set(true);
        }).join();
        assertThat(completed.get()).isTrue();
    }
}
