package com.example.producerconsumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * Default implementation of the ProducerConsumerService using BlockingQueue and thread pools.
 */
public class DefaultProducerConsumerService<T extends Item<?>> implements ProducerConsumerService<T> {
    private ProducerConsumerConfig config;
    private ExecutorService producerExecutor;
    private ExecutorService consumerExecutor;
    private BlockingQueue<T> queue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong itemsProduced = new AtomicLong(0);
    private final AtomicLong itemsConsumed = new AtomicLong(0);
    private long startTime;
    
    @Override
    public void start(ProducerConsumerConfig config, Supplier<T> producer, Consumer<T> consumer) {
        if (running.get()) {
            throw new IllegalStateException("Service is already running");
        }
        
        if (config == null || producer == null || consumer == null) {
            throw new IllegalArgumentException("Config, producer, and consumer cannot be null");
        }
        
        this.config = config;
        this.queue = new LinkedBlockingQueue<>(config.getQueueCapacity());
        this.producerExecutor = Executors.newFixedThreadPool(config.getNumberOfProducers());
        this.consumerExecutor = Executors.newFixedThreadPool(config.getNumberOfConsumers());
        this.startTime = System.currentTimeMillis();
        
        // Reset counters
        itemsProduced.set(0);
        itemsConsumed.set(0);
        
        running.set(true);
        
        // Start producer threads
        for (int i = 0; i < config.getNumberOfProducers(); i++) {
            final int producerId = i;
            producerExecutor.submit(() -> runProducer(producerId, producer, config.getProducerDelayMs()));
        }
        
        // Start consumer threads
        for (int i = 0; i < config.getNumberOfConsumers(); i++) {
            final int consumerId = i;
            consumerExecutor.submit(() -> runConsumer(consumerId, consumer, config.getConsumerDelayMs()));
        }
    }
    
    @Override
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        
        if (producerExecutor != null) {
            producerExecutor.shutdown();
        }
        if (consumerExecutor != null) {
            consumerExecutor.shutdown();
        }
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    @Override
    public ProducerConsumerConfig getConfig() {
        return config;
    }
    
    @Override
    public ProcessingStats getStats() {
        return new ProcessingStats(
            itemsProduced.get(),
            itemsConsumed.get(),
            startTime,
            System.currentTimeMillis(),
            queue != null ? queue.size() : 0
        );
    }
    
    private void runProducer(int producerId, Supplier<T> producer, long delayMs) {
        while (running.get()) {
            try {
                T item = producer.get();
                if (item != null) {
                    queue.put(item);
                    itemsProduced.incrementAndGet();
                }
                
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Producer " + producerId + " error: " + e.getMessage());
            }
        }
    }
    
    private void runConsumer(int consumerId, Consumer<T> consumer, long delayMs) {
        while (running.get()) {
            try {
                T item = queue.take();
                consumer.accept(item);
                itemsConsumed.incrementAndGet();
                
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Consumer " + consumerId + " error: " + e.getMessage());
            }
        }
    }
}