package com.batec.producerconsumer;

import com.batec.producerconsumer.azure.ResourceGraphConsumer;
import com.batec.producerconsumer.azure.ResourceGraphProducer;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureTest {

    @Test
    void consumeResourceGraph() {
        int consumers = 10;
        BlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>(5);
        Map<String, Object> poisonPill = Map.of("poison", "pill");
        ResourceGraphProducer<Map<String, Object>> producer = new ResourceGraphProducer<>(queue, poisonPill, consumers);
        Future<?>[] consumersFutures = new Future[consumers];
        try (ExecutorService executor = Executors.newFixedThreadPool(consumers)) {
            for (int i = 0; i < consumers; i++) {
                consumersFutures[i] = executor.submit(() -> {
                    ResourceGraphConsumer<Map<String, Object>> consumer = new ResourceGraphConsumer<>(queue, poisonPill);
                    consumer.startConsuming();
                });
            }
            producer.startProducing();
            for (Future<?> future : consumersFutures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        assertThat(queue).isEmpty();
    }
}
