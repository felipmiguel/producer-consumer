package com.batec.producerconsumer.azure;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resourcegraph.models.QueryRequestOptions;
import com.azure.resourcemanager.resourcegraph.models.QueryResponse;
import com.azure.resourcemanager.resourcegraph.models.ResultFormat;
import com.batec.producerconsumer.ConsumerQueue;
import com.batec.producerconsumer.WorkloadConfiguration;
import com.batec.producerconsumer.WorkloadCoordinator;
import com.batec.producerconsumer.ProducerQueue;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ResourceGraphProcessor {

    private static final AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);

    private final ResourceGraphManager graphManager = ResourceGraphManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), azureProfile);

    public void process() {
        WorkloadConfiguration<Map<String, Object>> workload = WorkloadConfiguration.<Map<String, Object>>builder()
                .bufferSize(10)
                .producerCount(1)
                .consumerCount(10)
                .producer(this::produce)
                .queueConsumer(this::consume)
                .build();
        WorkloadCoordinator.processWorkload(workload).join();
    }

    private void produce(ProducerQueue<Map<String, Object>> producerQueue) {
        final String query = "Resources";
        String skipToken = null;
        do {
            QueryRequestOptions requestOptions = new QueryRequestOptions()
                    .withResultFormat(ResultFormat.OBJECT_ARRAY);
            if (Objects.nonNull(skipToken)) {
                requestOptions.withSkipToken(skipToken);
            }
            QueryRequest request = new QueryRequest()
                    .withQuery(query)
                    .withOptions(requestOptions);
            QueryResponse response = graphManager.resourceProviders().resources(request);
            addResultsToQueue(producerQueue, response.data());
            skipToken = response.skipToken();
        } while (Objects.nonNull(skipToken));
        producerQueue.complete();
    }

    private void addResultsToQueue(ProducerQueue<Map<String, Object>> producerQueue, Object data) {
        if (Objects.nonNull(data) && (data instanceof Iterable<?> iterable)) {
            //noinspection unchecked
            for (Map<String, Object> item : (Iterable<Map<String, Object>>) iterable) {
                try {
                    producerQueue.put(item);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void consume(ConsumerQueue<Map<String, Object>> consumerQueue) {
        while (!consumerQueue.completed()) {
            try {
                Map<String, Object> item = consumerQueue.poll(10, TimeUnit.MILLISECONDS);
                if (item != null) {
                    processItem(item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processItem(Map<String, Object> item) {
        System.out.println("Processing item: " + item);
    }
}
