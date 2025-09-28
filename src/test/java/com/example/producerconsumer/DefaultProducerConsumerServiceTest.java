package com.example.producerconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

class DefaultProducerConsumerServiceTest {
    private DefaultProducerConsumerService<SimpleItem<String>> service;
    private ProducerConsumerConfig config;
    
    @BeforeEach
    void setUp() {
        service = new DefaultProducerConsumerService<>();
        config = new ProducerConsumerConfig(1, 1, 10, 100, 100);
    }
    
    @AfterEach
    void tearDown() {
        if (service.isRunning()) {
            service.stop();
        }
    }
    
    @Test
    void testServiceStartStop() {
        assertFalse(service.isRunning());
        
        Supplier<SimpleItem<String>> producer = () -> new SimpleItem<>("test");
        Consumer<SimpleItem<String>> consumer = (SimpleItem<String> item) -> {};
        
        service.start(config, producer, consumer);
        assertTrue(service.isRunning());
        assertEquals(config, service.getConfig());
        
        service.stop();
        assertFalse(service.isRunning());
    }
    
    @Test
    void testDoubleStart() {
        Supplier<SimpleItem<String>> producer = () -> new SimpleItem<>("test");
        Consumer<SimpleItem<String>> consumer = (SimpleItem<String> item) -> {};
        
        service.start(config, producer, consumer);
        assertThrows(IllegalStateException.class, 
            () -> service.start(config, producer, consumer));
    }
    
    @Test
    void testNullParameters() {
        Supplier<SimpleItem<String>> producer = () -> new SimpleItem<>("test");
        Consumer<SimpleItem<String>> consumer = (SimpleItem<String> item) -> {};
        
        assertThrows(IllegalArgumentException.class, 
            () -> service.start(null, producer, consumer));
        assertThrows(IllegalArgumentException.class, 
            () -> service.start(config, null, consumer));
        assertThrows(IllegalArgumentException.class, 
            () -> service.start(config, producer, null));
    }
    
    @Test
    void testBasicProducerConsumer() throws InterruptedException {
        final AtomicInteger producedCount = new AtomicInteger(0);
        final AtomicInteger consumedCount = new AtomicInteger(0);
        
        Supplier<SimpleItem<String>> producer = () -> {
            producedCount.incrementAndGet();
            return new SimpleItem<>("item-" + producedCount.get());
        };
        
        Consumer<SimpleItem<String>> consumer = (SimpleItem<String> item) -> {
            consumedCount.incrementAndGet();
        };
        
        ProducerConsumerConfig testConfig = new ProducerConsumerConfig(1, 1, 10, 50, 50);
        service.start(testConfig, producer, consumer);
        
        Thread.sleep(300); // Let it run for a bit
        
        ProcessingStats stats = service.getStats();
        assertTrue(stats.getItemsProduced() > 0);
        assertTrue(stats.getItemsConsumed() > 0);
        assertTrue(stats.getRunTimeMs() > 0);
        
        service.stop();
    }
    
    @Test
    void testStats() {
        ProcessingStats stats = service.getStats();
        assertEquals(0, stats.getItemsProduced());
        assertEquals(0, stats.getItemsConsumed());
        assertEquals(0, stats.getQueueSize());
    }
}