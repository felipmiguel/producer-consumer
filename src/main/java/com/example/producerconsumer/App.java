package com.example.producerconsumer;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Sample application demonstrating the producer-consumer system.
 */
public class App {
    private static final Random RANDOM = new Random();
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Producer-Consumer System Demo");
        System.out.println("============================");
        
        // Create configuration with 2 producers and 3 consumers
        ProducerConsumerConfig config = new ProducerConsumerConfig(
            2,    // producers
            3,    // consumers
            50   // queue capacity
        );
        
        System.out.println("Configuration: " + config);
        
        // Create the service
        ProducerConsumerService<SimpleItem<String>> service = new DefaultProducerConsumerService<>();
        
        // Define producer function - creates random message items
        Supplier<SimpleItem<String>> producer = () -> {
            String message = "Message-" + RANDOM.nextInt(1000);
            return new SimpleItem<>(message);
        };
        
        // Define consumer function - processes items
        Consumer<SimpleItem<String>> consumer = (SimpleItem<String> item) -> {
            System.out.printf("[%s] Processing: %s (data: %s)%n", 
                Thread.currentThread().getName(), 
                item.getId(), 
                item.getData());
        };
        
        // Start the system
        service.start(config, producer, consumer);
        
        // Let it run for 10 seconds and print stats every 2 seconds
        for (int i = 0; i < 5; i++) {
            Thread.sleep(2000);
            ProcessingStats stats = service.getStats();
            System.out.println("Stats: " + stats);
        }
        
        // Stop the system
        System.out.println("Stopping the system...");
        service.stop();
        
        // Final stats
        Thread.sleep(1000);
        ProcessingStats finalStats = service.getStats();
        System.out.println("Final stats: " + finalStats);
        
        System.out.println("Demo completed.");
    }
}