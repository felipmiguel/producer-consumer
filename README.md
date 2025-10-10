# Producer-Consumer System

A Java 21 project using Gradle that provides a flexible producer-consumer coordination framework for processing items with configurable number of producers and consumers.

## Features

- **Simple Coordination**: Easy-to-use `WorkloadCoordinator.processWorkload()` method for managing producer-consumer workflows
- **Configurable Concurrency**: Specify the number of producer and consumer threads
- **Thread-Safe**: Uses blocking queues for safe concurrent processing
- **Flexible Configuration**: Adjustable queue capacity via `WorkloadConfiguration`
- **Competing Consumer Pattern**: Multiple consumers compete for items from a shared queue
- **Automatic Completion Handling**: Built-in support for graceful shutdown via queue completion signals
- **Azure Integration**: Built-in support for Azure Resource Graph with pagination

## Project Structure

This project is organized into two Gradle modules:

### Core Library Module (`producer-consumer-core`)
The core producer-consumer framework as a reusable library:

```
producer-consumer-core/
└── src/main/java/com/batec/producerconsumer/
    ├── WorkloadCoordinator.java            # Main coordinator for producer-consumer workflows
    ├── WorkloadConfiguration.java          # Configuration class
    ├── ProducerQueue.java                  # Producer queue interface
    ├── ConsumerQueue.java                  # Consumer queue interface
    ├── ProducerConsumerQueue.java          # Combined queue interface
    └── DefaultProducerConsumerQueue.java   # Default queue implementation
```

### Samples Module (`producer-consumer-samples`)
Sample applications demonstrating library usage:

```
producer-consumer-samples/
└── src/main/java/com/batec/producerconsumer/
    ├── App.java                            # Demo application entry point
    └── azure/
        └── ResourceGraphProcessor.java     # Azure Resource Graph processor example
```

## Quick Start

### Running the Demo

```bash
./gradlew :producer-consumer-samples:run
```

This will run the Azure Resource Graph processor demo with 1 producer and 10 competing consumers.

### Building the Project

Build all modules:
```bash
./gradlew build
```

Build only the core library:
```bash
./gradlew :producer-consumer-core:build
```

Build only the samples:
```bash
./gradlew :producer-consumer-samples:build
```

### Running Tests

Run all tests:
```bash
./gradlew test
```

Run tests for core library:
```bash
./gradlew :producer-consumer-core:test
```

Run tests for samples (excluding Azure integration test):
```bash
./gradlew :producer-consumer-samples:test -DexcludeTags=ci-skip
```

## Usage

### Using the Core Library in Your Project

To use the producer-consumer-core library in your own Gradle project, add it as a dependency:

```gradle
dependencies {
    implementation project(':producer-consumer-core')
}
```

Or if published to a Maven repository:

```gradle
dependencies {
    implementation 'com.batec:producer-consumer-core:1.0.0'
}
```

### Recommended Approach: Using WorkloadCoordinator

The preferred way to implement producer-consumer workflows is using `WorkloadCoordinator.processWorkload()`:

```java
import com.batec.producerconsumer.*;
import java.util.concurrent.CompletableFuture;
import java.time.Duration;

// Create configuration using builder pattern
WorkloadConfiguration<String> config = WorkloadConfiguration.<String>builder()
    .producerCount(2)    // Number of producer threads
    .consumerCount(5)     // Number of consumer threads
    .bufferSize(50)       // Queue capacity
    .producerTerminationTimeout(Duration.ofSeconds(10))  // Wait up to 10 seconds for producers to terminate
    .consumerTerminationTimeout(Duration.ofSeconds(10))  // Wait up to 10 seconds for consumers to terminate
    .producer((ProducerQueue<String> queue) -> {
        try {
            for (int i = 0; i < 100; i++) {
                queue.put("Item-" + i);
            }
            queue.complete(); // Signal completion
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            queue.fail(e);
        }
    })
    .queueConsumer((ConsumerQueue<String> queue) -> {
        while (!queue.completed()) {
            try {
                String item = queue.take();
                System.out.println("Processing: " + item);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    })
    .build();

// Start the coordinator and wait for completion
CompletableFuture<Void> future = WorkloadCoordinator.processWorkload(config);
future.join(); // Wait for all producers and consumers to complete
```

### Azure Resource Graph Example

Complete example using the coordinator for Azure Resource Graph queries:

```java
import com.batec.producerconsumer.*;
import com.azure.resourcemanager.resourcegraph.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ResourceGraphProcessor {
    
    private final ResourceGraphManager graphManager = /* initialize */;
    
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
    
    private void produce(ProducerQueue<Map<String, Object>> queue) {
        String skipToken = null;
        do {
            // Query Azure Resource Graph
            QueryResponse response = graphManager.resourceProviders()
                .resources(createRequest(skipToken));
            
            // Add results to queue
            for (Map<String, Object> item : response.data()) {
                try {
                    queue.put(item);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            skipToken = response.skipToken();
        } while (skipToken != null);
        
        queue.complete(); // Signal completion
    }
    
    private void consume(ConsumerQueue<Map<String, Object>> queue) {
        while (!queue.completed()) {
            try {
                Map<String, Object> item = queue.poll(10, TimeUnit.MILLISECONDS);
                if (item != null) {
                    System.out.println("Processing: " + item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

## API Reference

### WorkloadCoordinator

The main coordination class for producer-consumer workflows.

**Method:**
- `static <T> CompletableFuture<Void> processWorkload(WorkloadConfiguration<T> configuration)`
  - Starts producer and consumer tasks based on the configuration
  - Returns a `CompletableFuture` that completes when all tasks are done
  - Automatically manages thread pools and resource cleanup

### WorkloadConfiguration<T>

Configuration object for producer-consumer workflows. Use the builder pattern to create instances.

**Builder Methods:**
- `builder()` - Creates a new builder instance
- `producer(Consumer<ProducerQueue<T>> producer)` - Function that produces items and adds them to the queue
- `queueConsumer(Consumer<ConsumerQueue<T>> queueConsumer)` - Function that consumes items from the queue
- `itemConsumer(Consumer<T> itemConsumer)` - Alternative consumer that processes individual items (either queueConsumer or itemConsumer must be provided)
- `producerCount(int count)` - Number of producer threads (default: 1)
- `consumerCount(int count)` - Number of consumer threads (default: 1)
- `bufferSize(int size)` - Maximum queue capacity (default: 1)
- `producerTerminationTimeout(Duration timeout)` - Timeout to await producer ExecutorService termination (default: 1 second)
- `consumerTerminationTimeout(Duration timeout)` - Timeout to await consumer ExecutorService termination (default: 1 second)
- `build()` - Builds and returns the configuration

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

## Requirements

- Java 17+ (configured for Java 17, designed for Java 21)
- Gradle 7.0+

## License

MIT License - see [LICENSE](LICENSE) file for details.