package com.batec.producerconsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Coordinates the producer and consumer using a shared queue.
 *
 */
public class WorkloadCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(WorkloadCoordinator.class);

    /**
     * Starts the producer and consumer tasks based on the provided configuration.
     *
     * @param <T>           The type of items being produced and consumed.
     * @param configuration The configuration for the producer-consumer process.
     * @return A CompletableFuture that completes when all producer and consumer tasks are done.
     */
    public static <T> CompletableFuture<Void> processWorkload(WorkloadConfiguration<T> configuration) {
        var producerExecutor = Executors.newFixedThreadPool(configuration.getProducerCount());
        var consumerExecutor = Executors.newFixedThreadPool(configuration.getConsumerCount());
        int producerCount = configuration.getProducerCount();
        int consumerCount = configuration.getConsumerCount();
        Consumer<ProducerQueue<T>> producer = configuration.getProducer();
        Consumer<ConsumerQueue<T>> consumer;
        if (configuration.getQueueConsumer() != null) {
            consumer = configuration.getQueueConsumer();
        } else {
            consumer = defaultConsumer(configuration.getItemConsumer());
        }
        int bufferSize = configuration.getBufferSize();
        DefaultProducerConsumerQueue<T> queue = new DefaultProducerConsumerQueue<>(bufferSize);


        List<CompletableFuture<?>> producerFutures = new ArrayList<>(producerCount);
        for (int i = 0; i < configuration.getProducerCount(); i++) {
            var producerFuture = CompletableFuture.runAsync(() -> producer.accept(queue), producerExecutor);
            producerFutures.add(producerFuture);
        }
        // When all producers are done, complete or fail the queue accordingly
        CompletableFuture<Void> allProducersDone = CompletableFuture.allOf(producerFutures.toArray(new CompletableFuture[0]))
                .handle((nothing, ex) -> {
                    if (ex != null) {
                        queue.fail(ex);
                    } else {
                        queue.complete();
                    }
                    return null;
                });

        List<CompletableFuture<?>> consumerFutures = new ArrayList<>(producerCount + consumerCount);
        for (int i = 0; i < consumerCount; i++) {
            consumerFutures.add(CompletableFuture.runAsync(() -> consumer.accept(queue), consumerExecutor));
        }

        return allProducersDone
                .thenCompose(nothing -> CompletableFuture.allOf(consumerFutures.toArray(new CompletableFuture[0])))
                .whenComplete((result, throwable) -> {
                    shutdownExecutors(producerExecutor, consumerExecutor, configuration);
                    if (throwable != null) {
                        LOG.error("Error occurred during processing", throwable);
                    } else {
                        LOG.debug("Processing completed successfully");
                    }
                });
    }

    private static <T> Consumer<ConsumerQueue<T>> defaultConsumer(Consumer<T> itemConsumer) {
        return consumerQueue -> {
            while (!consumerQueue.completed()) {
                try {
                    T item = consumerQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (item != null && itemConsumer != null) {
                        itemConsumer.accept(item);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            LOG.debug("Consumer finished processing " + Thread.currentThread().getName());
        };
    }

    private static <T> void shutdownExecutors(ExecutorService producerExecutor, ExecutorService consumerExecutor, WorkloadConfiguration<T> configuration) {
        LOG.debug("All tasks completed. Shutting down executors.");
        try {
            if (!producerExecutor.awaitTermination(
                    configuration.getProducerTerminationTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                producerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOG.error("Producer executor shutdown interrupted", e);
        }
        try {
            if (!consumerExecutor.awaitTermination(
                    configuration.getConsumerTerminationTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                consumerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOG.error("Consumer executor shutdown interrupted", e);
        }
    }
}
