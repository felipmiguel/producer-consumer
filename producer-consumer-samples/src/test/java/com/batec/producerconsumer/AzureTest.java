package com.batec.producerconsumer;

import com.batec.producerconsumer.azure.ResourceGraphProcessor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ci-skip")
public class AzureTest {

    @Test
    void processResourceGraph() {
        ResourceGraphProcessor processor = new ResourceGraphProcessor();
        processor.process();
    }
}
