package com.example.producerconsumer;

/**
 * Statistics about the producer-consumer system processing.
 */
public class ProcessingStats {
    private final long itemsProduced;
    private final long itemsConsumed;
    private final long startTime;
    private final long currentTime;
    private final int queueSize;
    
    /**
     * Creates processing statistics.
     * @param itemsProduced total number of items produced
     * @param itemsConsumed total number of items consumed
     * @param startTime system start time in milliseconds
     * @param currentTime current time in milliseconds
     * @param queueSize current queue size
     */
    public ProcessingStats(long itemsProduced, long itemsConsumed, long startTime, long currentTime, int queueSize) {
        this.itemsProduced = itemsProduced;
        this.itemsConsumed = itemsConsumed;
        this.startTime = startTime;
        this.currentTime = currentTime;
        this.queueSize = queueSize;
    }
    
    public long getItemsProduced() {
        return itemsProduced;
    }
    
    public long getItemsConsumed() {
        return itemsConsumed;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getCurrentTime() {
        return currentTime;
    }
    
    public long getRunTimeMs() {
        return currentTime - startTime;
    }
    
    public int getQueueSize() {
        return queueSize;
    }
    
    public double getProductionRate() {
        long runtime = getRunTimeMs();
        return runtime > 0 ? (itemsProduced * 1000.0) / runtime : 0.0;
    }
    
    public double getConsumptionRate() {
        long runtime = getRunTimeMs();
        return runtime > 0 ? (itemsConsumed * 1000.0) / runtime : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("ProcessingStats{produced=%d, consumed=%d, queue=%d, runtime=%dms, prodRate=%.2f/s, consRate=%.2f/s}", 
                           itemsProduced, itemsConsumed, queueSize, getRunTimeMs(), getProductionRate(), getConsumptionRate());
    }
}