package com.batec.producerconsumer.azure;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resourcegraph.models.QueryRequestOptions;
import com.azure.resourcemanager.resourcegraph.models.QueryResponse;
import com.azure.resourcemanager.resourcegraph.models.ResultFormat;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class ResourceGraphProducer<T> {
    private final BlockingQueue<T> resultsQueue;
    private final T poisonPill;
    private final int consumersCount;

    private static final AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);

    private final ResourceGraphManager graphManager = ResourceGraphManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), azureProfile);

    public ResourceGraphProducer(BlockingQueue<T> resultsQueue, T poisonPill, int consumersCount) {
        this.resultsQueue = resultsQueue;
        this.poisonPill = poisonPill;
        this.consumersCount = consumersCount;
    }

    public void startProducing() {
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
            addResultsToQueue(response.data());
            skipToken = response.skipToken();
        } while (Objects.nonNull(skipToken));

        notifyCompletion();
    }

    private void notifyCompletion() {
        // Indicate completion
        for (int i = 0; i < consumersCount; i++) {
            try {
                resultsQueue.put(poisonPill);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void addResultsToQueue(Object data) {
        if (Objects.nonNull(data) && (data instanceof Iterable<?> iterable)) {
            //noinspection unchecked
            for (T item : (Iterable<T>) iterable) {
                try {
                    resultsQueue.put(item);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
