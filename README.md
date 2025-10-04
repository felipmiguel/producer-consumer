# Producer-Consumer System

A Java 21 project using Gradle that provides a flexible producer-consumer coordination framework for processing items with configurable number of producers and consumers.

## Features

- **Simple Coordination**: Easy-to-use `ProducerConsumerCoordinator.doWork()` method for managing producer-consumer workflows
- **Configurable Concurrency**: Specify the number of producer and consumer threads
- **Thread-Safe**: Uses blocking queues for safe concurrent processing
- **Flexible Configuration**: Adjustable queue capacity via `ProcessConfiguration`
- **Competing Consumer Pattern**: Multiple consumers compete for items from a shared queue
- **Automatic Completion Handling**: Built-in support for graceful shutdown via queue completion signals
- **Azure Integration**: Built-in support for Azure Resource Graph with pagination

## Project Structure

```
src/main/java/com/batec/producerconsumer/
├── ProducerConsumerCoordinator.java    # Main coordinator for producer-consumer workflows
├── ProcessConfiguration.java           # Configuration class
├── ProducerQueue.java                  # Producer queue interface
├── ConsumerQueue.java                  # Consumer queue interface
├── ProducerConsumerQueue.java          # Combined queue interface
├── DefaultProducerConsumerQueue.java   # Default queue implementation
├── App.java                            # Demo application
└── azure/
    ├── ResourceGraphProcessor.java     # Azure Resource Graph processor using coordinator
    ├── ResourceGraphProducer.java      # Legacy producer with poison pill pattern
    └── ResourceGraphConsumer.java      # Legacy consumer with poison pill pattern
```

## Quick Start

### Running the Demo

```bash
./gradlew run
```

This will run the Azure Resource Graph processor demo with 1 producer and 10 competing consumers.

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

## Usage

### Recommended Approach: Using ProducerConsumerCoordinator

The preferred way to implement producer-consumer workflows is using `ProducerConsumerCoordinator.doWork()`:

```java
import com.batec.producerconsumer.*;
import java.util.concurrent.CompletableFuture;

// Create configuration
ProcessConfiguration<String> config = new ProcessConfiguration<>();
config.setProducerCount(2);    // Number of producer threads
config.setConsumerCount(5);     // Number of consumer threads
config.setBufferSize(50);       // Queue capacity
config.setProducerTerminationTimeout(10);  // Wait up to 10 seconds for producers to terminate
config.setConsumerTerminationTimeout(10);  // Wait up to 10 seconds for consumers to terminate

// Define producer function
config.setProducer((ProducerQueue<String> queue) -> {
    try {
        for (int i = 0; i < 100; i++) {
            queue.put("Item-" + i);
        }
        queue.complete(); // Signal completion
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        queue.fail(e);
    }
});

// Define consumer function
config.setConsumer((ConsumerQueue<String> queue) -> {
    while (!queue.completed()) {
        try {
            String item = queue.take();
            System.out.println("Processing: " + item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
});

// Start the coordinator and wait for completion
CompletableFuture<Void> future = ProducerConsumerCoordinator.doWork(config);
future.join(); // Wait for all producers and consumers to complete
```

### Azure Resource Graph Example

Complete example using the coordinator for Azure Resource Graph queries:

```java
import com.batec.producerconsumer.*;
import com.azure.resourcemanager.resourcegraph.*;
import java.util.Map;

public class ResourceGraphProcessor {
    
    private final ResourceGraphManager graphManager = /* initialize */;
    
    public void process() {
        ProcessConfiguration<Map<String, Object>> config = new ProcessConfiguration<>();
        config.setBufferSize(10);
        config.setProducerCount(1);
        config.setConsumerCount(10);
        config.setProducer(this::produce);
        config.setConsumer(this::consume);
        
        // Execute and wait for completion
        ProducerConsumerCoordinator.doWork(config).join();
    }
    
    private void produce(ProducerQueue<Map<String, Object>> queue) {
        String skipToken = null;
        do {
            // Query Azure Resource Graph
            QueryResponse response = graphManager.resourceProviders()
                .resources(createRequest(skipToken));
            
            // Add results to queue
            for (Map<String, Object> item : response.data()) {
                queue.put(item);
            }
            skipToken = response.skipToken();
        } while (skipToken != null);
        
        queue.complete(); // Signal completion
    }
    
    private void consume(ConsumerQueue<Map<String, Object>> queue) {
        while (!queue.completed()) {
            try {
                Map<String, Object> item = queue.take();
                System.out.println("Processing: " + item);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

### Alternative: Legacy Poison Pill Pattern

The project also includes a legacy implementation using the poison pill pattern for explicit control:

```java
import com.batec.producerconsumer.azure.ResourceGraphConsumer;
import com.batec.producerconsumer.azure.ResourceGraphProducer;
import java.util.Map;
import java.util.concurrent.*;

// Configure the number of competing consumers
int numberOfConsumers = 10;

// Create a shared queue for all consumers
BlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>(100);

// Define a poison pill to signal completion
Map<String, Object> poisonPill = Map.of("poison", "pill");

// Create the producer with consumer count (for poison pills)
ResourceGraphProducer<Map<String, Object>> producer = 
    new ResourceGraphProducer<>(queue, poisonPill, numberOfConsumers);

// Start consumer threads
ExecutorService executor = Executors.newFixedThreadPool(numberOfConsumers);
for (int i = 0; i < numberOfConsumers; i++) {
    executor.submit(() -> {
        ResourceGraphConsumer<Map<String, Object>> consumer = 
            new ResourceGraphConsumer<>(queue, poisonPill);
        consumer.startConsuming();
    });
}

// Start producing (this will query Azure Resource Graph)
producer.startProducing();

// Wait for all consumers to finish
executor.shutdown();
executor.awaitTermination(1, TimeUnit.HOURS);
```

**Note:** This approach is maintained for backward compatibility. The `ProducerConsumerCoordinator.doWork()` method is the preferred approach for new code.

## API Reference

### ProducerConsumerCoordinator

The main coordination class for producer-consumer workflows.

**Method:**
- `static <T> CompletableFuture<Void> doWork(ProcessConfiguration<T> configuration)`
  - Starts producer and consumer tasks based on the configuration
  - Returns a `CompletableFuture` that completes when all tasks are done
  - Automatically manages thread pools and resource cleanup

### ProcessConfiguration<T>

Configuration object for producer-consumer workflows.

**Properties:**
- `producer: Consumer<ProducerQueue<T>>` - Function that produces items and adds them to the queue
- `consumer: Consumer<ConsumerQueue<T>>` - Function that consumes items from the queue
- `producerCount: int` - Number of producer threads
- `consumerCount: int` - Number of consumer threads
- `bufferSize: int` - Maximum queue capacity
- `producerTerminationTimeout: long` - Timeout in seconds to await producer ExecutorService termination (default: 1 second)
- `consumerTerminationTimeout: long` - Timeout in seconds to await consumer ExecutorService termination (default: 1 second)

### ProducerQueue<T>

Interface for producers to add items to the queue.

**Methods:**
- `put(T item)` - Adds an item to the queue (blocks if queue is full)
- `complete()` - Signals that production is complete
- `fail(Throwable t)` - Signals that production failed with an error

### ConsumerQueue<T>

Interface for consumers to retrieve items from the queue.

**Methods:**
- `take()` - Retrieves and removes an item from the queue (blocks if queue is empty)
- `completed()` - Returns `true` if production is complete and queue is empty

### Legacy Classes (Backward Compatibility)

#### ResourceGraphProducer<T>

Legacy producer for Azure Resource Graph queries using poison pill pattern:

- `ResourceGraphProducer(queue, poisonPill, consumersCount)` - Constructor
  - `queue` - Shared BlockingQueue for all consumers
  - `poisonPill` - Sentinel value to signal completion
  - `consumersCount` - Number of consumers (determines poison pills to send)
- `startProducing()` - Queries Azure Resource Graph and adds results to queue with pagination support

#### ResourceGraphConsumer<T>

Legacy consumer that processes items from a shared queue:

- `ResourceGraphConsumer(queue, poisonPill)` - Constructor
  - `queue` - Shared BlockingQueue with producer and other consumers
  - `poisonPill` - Sentinel value that signals when to stop consuming
- `startConsuming()` - Continuously processes items until receiving poison pill

## Requirements

- Java 17+ (configured for Java 17, designed for Java 21)
- Gradle 7.0+

## License

MIT License - see [LICENSE](LICENSE) file for details.