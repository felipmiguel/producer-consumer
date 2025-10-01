package com.batec.producerconsumer;

import com.batec.producerconsumer.azure.ResourceGraphProcessor;

/**
 * Sample application demonstrating the producer-consumer system.
 */
public class App {

    public static void main(String[] args) {
        ResourceGraphProcessor processor = new ResourceGraphProcessor();
        processor.process();
    }
}