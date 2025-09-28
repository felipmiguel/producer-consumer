package com.example.producerconsumer;

import java.util.UUID;

/**
 * Simple implementation of the Item interface.
 * @param <T> the type of data contained in the item
 */
public class SimpleItem<T> implements Item<T> {
    private final String id;
    private final T data;
    private final long timestamp;
    
    /**
     * Creates a new SimpleItem with the specified data.
     * @param data the data to store in this item
     */
    public SimpleItem(T data) {
        this.id = UUID.randomUUID().toString();
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates a new SimpleItem with the specified ID and data.
     * @param id the unique identifier for this item
     * @param data the data to store in this item
     */
    public SimpleItem(String id, T data) {
        this.id = id;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public T getData() {
        return data;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("SimpleItem{id='%s', data=%s, timestamp=%d}", id, data, timestamp);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SimpleItem<?> that = (SimpleItem<?>) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}