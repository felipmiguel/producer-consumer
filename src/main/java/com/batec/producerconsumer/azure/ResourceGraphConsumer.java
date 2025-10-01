package com.batec.producerconsumer.azure;

import java.util.concurrent.BlockingQueue;

public class ResourceGraphConsumer<T> {

    private final BlockingQueue<T> pendingQueue;
    private final T poisonPill;

    public ResourceGraphConsumer(BlockingQueue<T> pendingQueue, T poisonPill) {
        this.pendingQueue = pendingQueue;
        this.poisonPill = poisonPill;
    }

    public void startConsuming() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                T item = pendingQueue.take();
                if (item.equals(poisonPill)) {
                    break; // Exit on poison pill
                }
                processItem(item);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processItem(T item) {
        System.out.println("Processing item: " + item);
    }
}
