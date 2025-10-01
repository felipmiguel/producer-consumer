# Producer-Consumer System

A Java 21 project using Gradle that provides a generic producer-consumer interface for processing items with configurable number of producers and consumers.

## Features

- **Generic Interface**: Process any type of items that implement the `Item<T>` interface
- **Configurable Concurrency**: Specify the number of producer and consumer threads
- **Thread-Safe**: Uses blocking queues for safe concurrent processing
- **Statistics**: Real-time monitoring of production/consumption rates and queue status
- **Flexible Configuration**: Adjustable queue capacity and processing delays

## Project Structure

```
src/main/java/com/example/producerconsumer/
├── Item.java                           # Generic item interface
├── ProducerConsumerService.java        # Main service interface
├── DefaultProducerConsumerService.java # Default implementation
├── ProducerConsumerConfig.java         # Configuration class
├── ProcessingStats.java                # Statistics tracking
├── SimpleItem.java                     # Simple item implementation
└── App.java                            # Demo application
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
service.

        start(config, producer, consumer);

        // Monitor statistics
        ProcessingStats stats = service.getStats();
System.out.

        println("Stats: "+stats);

// Stop the system
service.

        stop();
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

## Requirements

- Java 17+ (configured for Java 17, designed for Java 21)
- Gradle 7.0+

## License

MIT License - see [LICENSE](LICENSE) file for details.