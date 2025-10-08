package com.batec.producerconsumer;

import com.batec.producerconsumer.azure.ResourceGraphConsumer;
import com.batec.producerconsumer.azure.ResourceGraphProcessor;
import com.batec.producerconsumer.azure.ResourceGraphProducer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ci-skip")
public class AzureTest {

    @Test
    void consumeResourceGraph() {
        int consumers = 10;
        BlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>(5);
        Map<String, Object> poisonPill = Map.of("poison", "pill");
        ResourceGraphProducer<Map<String, Object>> producer = new ResourceGraphProducer<>(queue, poisonPill, consumers);
        Future<?>[] consumersFutures = new Future[consumers];
        ExecutorService executor = Executors.newFixedThreadPool(consumers);
        try {
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

        } finally {
            executor.shutdown();
        }
        assertThat(queue).isEmpty();
    }

    @Test
    void processResourceGraph() {
        ResourceGraphProcessor processor = new ResourceGraphProcessor();
        processor.process();
    }
}
