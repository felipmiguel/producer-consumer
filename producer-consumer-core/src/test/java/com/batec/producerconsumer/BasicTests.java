package com.batec.producerconsumer;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicTests {

    @Test
    void testProducerQueueConsumer() {
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        WorkloadConfiguration<Integer> config = WorkloadConfiguration.<Integer>builder()
                .bufferSize(10)
                .producerCount(5)
                .consumerCount(20)
                .producer(producerTask(producedCount))
                .queueConsumer(consumerTask(consumedCount))
                .build();
        WorkloadCoordinator.processWorkload(config).thenAccept(nothing -> {
            assertThat(producedCount.get()).isEqualTo(consumedCount.get());
            completed.set(true);
        }).join();
        assertThat(completed.get()).isTrue();
    }

    @Test
    void testProducerItemConsumer() {
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        WorkloadConfiguration<Integer> config = WorkloadConfiguration.<Integer>builder()
                .bufferSize(10)
                .producerCount(5)
                .consumerCount(20)
                .producer(producerTask(producedCount))
                .itemConsumer( item -> {
                    consumedCount.incrementAndGet();
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15)); // Random sleep between 5-14 ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } )
                .build();
        WorkloadCoordinator.processWorkload(config).thenAccept(nothing -> {
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

    private static Consumer<ProducerQueue<Integer>> producerTask(AtomicInteger producedCount) {
        return producerQueue -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    producerQueue.put(i);
                    producedCount.incrementAndGet();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(2, 5)); // Random sleep between 2-5 ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    @Test
    void testConfigurationBuilderValidation() {
        // Test invalid buffer size
        try {
            WorkloadConfiguration.<String>builder()
                    .bufferSize(0)
                    .producer(producerQueue -> {})
                    .queueConsumer(consumerQueue -> {})
                    .build();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Buffer size must be greater than 0");
        }

        // Test invalid producer count
        try {
            WorkloadConfiguration.<String>builder()
                    .bufferSize(10)
                    .producerCount(0)
                    .producer(producerQueue -> {})
                    .queueConsumer(consumerQueue -> {})
                    .build();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Producer count must be greater than 0");
        }

        // Test invalid consumer count
        try {
            WorkloadConfiguration.<String>builder()
                    .bufferSize(10)
                    .producerCount(1)
                    .consumerCount(0)
                    .producer(producerQueue -> {})
                    .queueConsumer(consumerQueue -> {})
                    .build();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Consumer count must be greater than 0");
        }

        // Test missing producer
        try {
            WorkloadConfiguration.<String>builder()
                    .bufferSize(10)
                    .producerCount(1)
                    .consumerCount(1)
                    .queueConsumer(consumerQueue -> {})
                    .build();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Producer function must be provided");
        }

        // Test missing consumer and item consumer
        try {
            WorkloadConfiguration.<String>builder()
                    .bufferSize(10)
                    .producerCount(1)
                    .consumerCount(1)
                    .producer(producerQueue -> {})
                    .build();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Either queueConsumer or itemConsumer must be provided");
        }
    }

    @Test
    void testTimeoutConfiguration() {
        WorkloadConfiguration<String> config = WorkloadConfiguration.<String>builder()
                .bufferSize(5)
                .producerCount(1)
                .consumerCount(1)
                .consumerTerminationTimeout(Duration.ofMillis(5))
                .producerTerminationTimeout(Duration.ofMillis(10))
                .producer(producerQueue -> {
                    for (int i = 0; i < 10; i++) {
                        try {
                            producerQueue.put("Item-" + i);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                })
                .queueConsumer(consumerQueue -> {
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
                })
                .build();

        // Verify the timeout values are set correctly
        assertThat(config.getConsumerTerminationTimeout()).isEqualTo(Duration.ofMillis(5));
        assertThat(config.getProducerTerminationTimeout()).isEqualTo(Duration.ofMillis(10));


        // This should complete without issues using the configured timeouts
        WorkloadCoordinator.processWorkload(config).join();
    }

    @Test
    void whenExceptionHappensInProducer_thenJobIsCompleted() {
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        WorkloadConfiguration<Integer> config = WorkloadConfiguration.<Integer>builder()
                .bufferSize(10)
                .producerCount(1)
                .consumerCount(20)
                .producer(producerFailingTask(producedCount))
                .queueConsumer(consumerTask(consumedCount))
                .build();

        // when there is an exception we expect the future to complete exceptionally
        // we want to verify that producedCount == consumedCount at that point
        // and the task finished as it's marked as completed by the ProducerConsumerCoordinator
        WorkloadCoordinator.processWorkload(config)
                .exceptionally(e -> {
                    assertThat(producedCount.get()).isEqualTo(consumedCount.get());
                    completed.set(true);
                    return null;
                }).join();
        assertThat(completed.get()).isTrue();
    }
}
