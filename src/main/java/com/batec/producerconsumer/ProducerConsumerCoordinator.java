package com.batec.producerconsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Coordinates the producer and consumer using a shared queue.
 *
 */
public class ProducerConsumerCoordinator {
    /**
     * Starts the producer and consumer tasks based on the provided configuration.
     *
     * @param <T>           The type of items being produced and consumed.
     * @param configuration The configuration for the producer-consumer process.
     * @return A CompletableFuture that completes when all producer and consumer tasks are done.
     */
    public static <T> CompletableFuture<Void> doWork(ProcessConfiguration<T> configuration) {
        try (var producerExecutor = Executors.newFixedThreadPool(configuration.getProducerCount());
             var consumerExecutor = Executors.newFixedThreadPool(configuration.getConsumerCount())) {
            int producerCount = configuration.getProducerCount();
            int consumerCount = configuration.getConsumerCount();
            Consumer<ProducerQueue<T>> producer = configuration.getProducer();
            Consumer<ConsumerQueue<T>> consumer = configuration.getConsumer();
            int bufferSize = configuration.getBufferSize();
            DefaultProducerConsumerQueue<T> queue = new DefaultProducerConsumerQueue<>(bufferSize);

            List<CompletableFuture<?>> futures = new ArrayList<>(producerCount + consumerCount);
            for (int i = 0; i < configuration.getProducerCount(); i++) {
                futures.add(CompletableFuture.runAsync(() -> producer.accept(queue), producerExecutor));
            }

            for (int i = 0; i < consumerCount; i++) {
                futures.add(CompletableFuture.runAsync(() -> consumer.accept(queue), consumerExecutor));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }
    }
}
