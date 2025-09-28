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
        assertEquals(1000, config.getProducerDelayMs());
        assertEquals(1000, config.getConsumerDelayMs());
    }
    
    @Test
    void testCustomConfig() {
        ProducerConsumerConfig config = new ProducerConsumerConfig(2, 3, 50, 500, 800);
        assertEquals(2, config.getNumberOfProducers());
        assertEquals(3, config.getNumberOfConsumers());
        assertEquals(50, config.getQueueCapacity());
        assertEquals(500, config.getProducerDelayMs());
        assertEquals(800, config.getConsumerDelayMs());
    }
    
    @Test
    void testInvalidProducers() {
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(0, 1, 10, 0, 0));
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(-1, 1, 10, 0, 0));
    }
    
    @Test
    void testInvalidConsumers() {
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(1, 0, 10, 0, 0));
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(1, -1, 10, 0, 0));
    }
    
    @Test
    void testInvalidQueueCapacity() {
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(1, 1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, 
            () -> new ProducerConsumerConfig(1, 1, -1, 0, 0));
    }
    
    @Test
    void testNegativeDelays() {
        // Negative delays should be converted to 0
        ProducerConsumerConfig config = new ProducerConsumerConfig(1, 1, 10, -100, -200);
        assertEquals(0, config.getProducerDelayMs());
        assertEquals(0, config.getConsumerDelayMs());
    }
    
    @Test
    void testToString() {
        ProducerConsumerConfig config = new ProducerConsumerConfig(2, 3, 50, 500, 800);
        String toString = config.toString();
        assertTrue(toString.contains("producers=2"));
        assertTrue(toString.contains("consumers=3"));
        assertTrue(toString.contains("queueCapacity=50"));
    }
}