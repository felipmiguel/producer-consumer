package com.batec.producerconsumer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicTests {

    @Test
    void testProducerConsumer() {
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        ProcessConfiguration<Integer> config = new ProcessConfiguration<>();
        config.setBufferSize(10);
        config.setProducerCount(5);
        config.setConsumerCount(20);
        config.setProducer(producerTask(producedCount));
        config.setConsumer(consumerTask(consumedCount));
        ProducerConsumerCoordinator.doWork(config).thenAccept(nothing -> {
            assertThat(producedCount.get()).isEqualTo(consumedCount.get());
            completed.set(true);
        }).join();
        assertThat(completed.get()).isTrue();
    }

    private static Consumer<ConsumerQueue<Integer>> consumerTask(AtomicInteger consumedCount) {
        return consumerQueue -> {
            while (!consumerQueue.completed()) {
                try {
                    Integer item = consumerQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (item != null) {
                        consumedCount.incrementAndGet();
                        Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15)); // Random sleep between 5-14 ms
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("Consumer finished processing" + Thread.currentThread().getName());
        };
    }

    private static Consumer<ProducerQueue<Integer>> producerTask(AtomicInteger producedCount) {
        return producerQueue -> {
            for (int i = 0; i < 10000; i++) {
                try {
                    producerQueue.put(i);
                    producedCount.incrementAndGet();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(2, 5)); // Random sleep between 2-5 ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            producerQueue.complete();
        };
    }

    private static Consumer<ProducerQueue<Integer>> producerFailingTask(AtomicInteger producedCount) {
        return producerQueue -> {
            for (int i = 0; i < 10; i++) {
                try {
                    producerQueue.put(i);
                    producedCount.incrementAndGet();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(2, 5)); // Random sleep between 2-5 ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            throw new RuntimeException("Simulated producer failure");
            // we don't call complete() here
        };
    }

    @Test
    void testTimeoutConfiguration() {
        ProcessConfiguration<String> config = new ProcessConfiguration<>();
        config.setBufferSize(5);
        config.setProducerCount(1);
        config.setConsumerCount(1);
        config.setProducerTerminationTimeout(10);
        config.setConsumerTerminationTimeout(5);

        // Verify the timeout values are set correctly
        assertThat(config.getProducerTerminationTimeout()).isEqualTo(10);
        assertThat(config.getConsumerTerminationTimeout()).isEqualTo(5);

        config.setProducer(producerQueue -> {
            for (int i = 0; i < 10; i++) {
                try {
                    producerQueue.put("Item-" + i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            producerQueue.complete();
        });
        config.setConsumer(consumerQueue -> {
            while (!consumerQueue.completed()) {
                try {
                    String item = consumerQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (item != null) {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(2, 5)); // Random sleep between 2-5 ms
                        // Process item
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // This should complete without issues using the configured timeouts
        ProducerConsumerCoordinator.doWork(config).join();
    }

    @Test
    void whenExceptionHappensInProducer_thenJobIsCompleted() {
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        ProcessConfiguration<Integer> config = new ProcessConfiguration<>();
        config.setBufferSize(10);
        config.setProducerCount(1);
        config.setConsumerCount(20);
        // producer fails after producing 10 items
        config.setProducer(producerFailingTask(producedCount));
        config.setConsumer(consumerTask(consumedCount));

        // when there is an exception we expect the future to complete exceptionally
        // we want to verify that producedCount == consumedCount at that point
        // and the task finished as it's marked as completed by the ProducerConsumerCoordinator
        ProducerConsumerCoordinator.doWork(config)
                .exceptionally(e -> {
                    assertThat(producedCount.get()).isEqualTo(consumedCount.get());
                    completed.set(true);
                    return null;
                }).join();
        assertThat(completed.get()).isTrue();


    }
}
