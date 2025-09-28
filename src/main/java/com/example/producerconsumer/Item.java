package com.example.producerconsumer;

/**
 * Generic interface for items that can be processed by the producer-consumer system.
 * @param <T> the type of data contained in the item
 */
public interface Item<T> {
    /**
     * Gets the data contained in this item.
     * @return the item data
     */
    T getData();
    
    /**
     * Gets a unique identifier for this item.
     * @return the item ID
     */
    String getId();
    
    /**
     * Gets the timestamp when this item was created.
     * @return the creation timestamp in milliseconds
     */
    long getTimestamp();
}