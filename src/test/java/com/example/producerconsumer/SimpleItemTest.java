package com.example.producerconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class SimpleItemTest {
    private SimpleItem<String> item;
    
    @BeforeEach
    void setUp() {
        item = new SimpleItem<>("test data");
    }
    
    @Test
    void testItemCreation() {
        assertNotNull(item.getId());
        assertEquals("test data", item.getData());
        assertTrue(item.getTimestamp() > 0);
    }
    
    @Test
    void testItemWithCustomId() {
        SimpleItem<String> customItem = new SimpleItem<>("custom-id", "data");
        assertEquals("custom-id", customItem.getId());
        assertEquals("data", customItem.getData());
    }
    
    @Test
    void testEquals() {
        SimpleItem<String> item1 = new SimpleItem<>("same-id", "data1");
        SimpleItem<String> item2 = new SimpleItem<>("same-id", "data2");
        assertEquals(item1, item2);
    }
    
    @Test
    void testHashCode() {
        SimpleItem<String> item1 = new SimpleItem<>("same-id", "data1");
        SimpleItem<String> item2 = new SimpleItem<>("same-id", "data2");
        assertEquals(item1.hashCode(), item2.hashCode());
    }
    
    @Test
    void testToString() {
        String toString = item.toString();
        assertTrue(toString.contains("SimpleItem"));
        assertTrue(toString.contains("test data"));
        assertTrue(toString.contains(item.getId()));
    }
}