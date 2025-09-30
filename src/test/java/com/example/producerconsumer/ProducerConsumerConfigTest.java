package com.example.producerconsumer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProducerConsumerConfigTest {
    
    @Test
    void testDefaultConfig() {
        ProducerConsumerConfig config = new ProducerConsumerConfig();
        assertEquals(1, config.getNumberOfProducers());
        assertEquals(1, config.getNumberOfConsumers());
        assertEquals(100, config.getQueueCapacity());
    }
    
    @Test
    void testCustomConfig() {
        ProducerConsumerConfig config = new ProducerConsumerConfig(2, 3, 50);
        assertEquals(2, config.getNumberOfProducers());
        assertEquals(3, config.getNumberOfConsumers());
        assertEquals(50, config.getQueueCapacity());
    }
    
    @Test
    void testInvalidProducers() {
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(0, 1, 10));
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(-1, 1, 10));
    }
    
    @Test
    void testInvalidConsumers() {
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(1, 0, 10));
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(1, -1, 10));
    }
    
    @Test
    void testInvalidQueueCapacity() {
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(1, 1, 0));
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(1, 1, -1));
    }
    
    @Test
    void testToString() {
        ProducerConsumerConfig config = new ProducerConsumerConfig(2, 3, 50);
        String toString = config.toString();
        assertTrue(toString.contains("producers=2"));
        assertTrue(toString.contains("consumers=3"));
        assertTrue(toString.contains("queueCapacity=50"));
    }
}