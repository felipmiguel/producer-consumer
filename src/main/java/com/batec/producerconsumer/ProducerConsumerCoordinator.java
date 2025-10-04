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
public class ProducerConsumerCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerConsumerCoordinator.class);

    /**
     * Starts the producer and consumer tasks based on the provided configuration.
     *
     * @param <T>           The type of items being produced and consumed.
     * @param configuration The configuration for the producer-consumer process.
     * @return A CompletableFuture that completes when all producer and consumer tasks are done.
     */
    public static <T> CompletableFuture<Void> doWork(ProcessConfiguration<T> configuration) {
        var producerExecutor = Executors.newFixedThreadPool(configuration.getProducerCount());
        var consumerExecutor = Executors.newFixedThreadPool(configuration.getConsumerCount());
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

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//        return allDone.handle((result, throwable) -> {
//            shutdownExecutors(producerExecutor, consumerExecutor);
//            if (throwable != null) {
//                LOG.error("Error occurred during processing", throwable);
//                queue.fail(throwable);
//            } else {
//                LOG.debug("Processing completed successfully");
//                queue.complete();
//            }
//            return null;
//        });
        return allDone.whenComplete((result, throwable) -> {
            shutdownExecutors(producerExecutor, consumerExecutor);
            if( throwable != null) {
                LOG.error("Error occurred during processing", throwable);
            } else {
                LOG.debug("Processing completed successfully");
            }
        });

    }

    private static void shutdownExecutors(ExecutorService producerExecutor, ExecutorService consumerExecutor) {
        LOG.debug("All tasks completed. Shutting down executors.");
        producerExecutor.shutdown();
        consumerExecutor.shutdown();
        try {
            if (!producerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                producerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOG.error("Producer executor shutdown interrupted", e);
        }
        try {
            if (!consumerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                consumerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOG.error("Consumer executor shutdown interrupted", e);
        }
    }
}
