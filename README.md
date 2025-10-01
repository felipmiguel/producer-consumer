# Producer-Consumer System

A Java 21 project using Gradle that provides a generic producer-consumer interface for processing items with configurable number of producers and consumers.

## Features

- **Generic Interface**: Process any type of items that implement the `Item<T>` interface
- **Configurable Concurrency**: Specify the number of producer and consumer threads
- **Thread-Safe**: Uses blocking queues for safe concurrent processing
- **Statistics**: Real-time monitoring of production/consumption rates and queue status
- **Flexible Configuration**: Adjustable queue capacity and processing delays
- **Competing Consumer Pattern**: Direct BlockingQueue implementation with poison pill pattern for graceful shutdown
- **Azure Integration**: Built-in support for Azure Resource Graph with pagination

## Project Structure

```
src/main/java/com/example/producerconsumer/
├── Item.java                           # Generic item interface
├── ProducerConsumerService.java        # Main service interface
├── DefaultProducerConsumerService.java # Default implementation
├── ProducerConsumerConfig.java         # Configuration class
├── ProcessingStats.java                # Statistics tracking
├── SimpleItem.java                     # Simple item implementation
├── App.java                            # Demo application
└── azure/
    ├── ResourceGraphProducer.java      # Azure Resource Graph producer
    └── ResourceGraphConsumer.java      # Competing consumer implementation
```

## Quick Start

### Running the Demo

```bash
./gradlew run
```

This will run a demo with 2 producers and 3 consumers processing string messages.

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

## Usage

### Basic Example

```java
import com.example.producerconsumer.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Create configuration: 2 producers, 3 consumers
ProducerConsumerConfig config = new ProducerConsumerConfig(
    2,    // number of producers
    3,    // number of consumers
    50,   // queue capacity
    500,  // producer delay (ms)
    800   // consumer delay (ms)
);

// Create service
ProducerConsumerService<SimpleItem<String>> service = new DefaultProducerConsumerService<>();

// Define producer function
Supplier<SimpleItem<String>> producer = () -> {
    String data = "Item-" + System.currentTimeMillis();
    return new SimpleItem<>(data);
};

// Define consumer function
Consumer<SimpleItem<String>> consumer = (item) -> {
    System.out.println("Processing: " + item.getData());
};

// Start the system
service.start(config, producer, consumer);

// Monitor statistics
ProcessingStats stats = service.getStats();
System.out.println("Stats: " + stats);

// Stop the system
service.stop();
```

### Custom Item Implementation

```java
public class MyCustomItem implements Item<MyData> {
    private final String id;
    private final MyData data;
    private final long timestamp;
    
    public MyCustomItem(MyData data) {
        this.id = UUID.randomUUID().toString();
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public MyData getData() { return data; }
    
    @Override
    public String getId() { return id; }
    
    @Override
    public long getTimestamp() { return timestamp; }
}
```

### Competing Consumer Pattern (Azure Resource Graph Example)

The project also includes a direct BlockingQueue-based implementation using the competing consumer pattern with poison pill for graceful shutdown. This approach is demonstrated with Azure Resource Graph integration:

```java
import com.example.producerconsumer.azure.ResourceGraphConsumer;
import com.example.producerconsumer.azure.ResourceGraphProducer;
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

**Key Features of this Pattern:**
- **Competing Consumers**: Multiple consumer threads process items from a single shared queue
- **Poison Pill Pattern**: Producer sends a poison pill for each consumer to signal completion
- **Direct BlockingQueue**: Uses `BlockingQueue.take()` and `put()` for thread-safe operations
- **No Wrapper Interface**: Works directly with your data types (e.g., `Map<String, Object>`)
- **Azure Integration**: Designed for processing Azure Resource Graph query results with pagination support

## API Reference

### ProducerConsumerService Interface

- `start(config, producer, consumer)` - Starts the producer-consumer system
- `stop()` - Gracefully stops the system
- `isRunning()` - Checks if the system is running
- `getConfig()` - Gets current configuration
- `getStats()` - Gets processing statistics

### ProducerConsumerConfig

- `numberOfProducers` - Number of producer threads
- `numberOfConsumers` - Number of consumer threads  
- `queueCapacity` - Maximum queue size
- `producerDelayMs` - Delay between productions
- `consumerDelayMs` - Delay between consumptions

### ProcessingStats

- `itemsProduced` - Total items produced
- `itemsConsumed` - Total items consumed
- `queueSize` - Current queue size
- `runTimeMs` - Total runtime
- `productionRate` - Items produced per second
- `consumptionRate` - Items consumed per second

### ResourceGraphProducer (Azure Integration)

A specialized producer for Azure Resource Graph queries:

- `ResourceGraphProducer(queue, poisonPill, consumersCount)` - Constructor
  - `queue` - Shared BlockingQueue for all consumers
  - `poisonPill` - Sentinel value to signal completion
  - `consumersCount` - Number of consumers (determines poison pills to send)
- `startProducing()` - Queries Azure Resource Graph and adds results to queue with pagination support

### ResourceGraphConsumer (Competing Consumer)

A consumer that processes items from a shared queue:

- `ResourceGraphConsumer(queue, poisonPill)` - Constructor
  - `queue` - Shared BlockingQueue with producer and other consumers
  - `poisonPill` - Sentinel value that signals when to stop consuming
- `startConsuming()` - Continuously processes items until receiving poison pill

## Requirements

- Java 17+ (configured for Java 17, designed for Java 21)
- Gradle 7.0+

## License

MIT License - see [LICENSE](LICENSE) file for details.