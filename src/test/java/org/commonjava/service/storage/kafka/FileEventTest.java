package org.commonjava.service.storage.kafka;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import org.commonjava.event.file.FileEvent;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

@Ignore
@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
public class FileEventTest
{
    @Inject @Any
    InMemoryConnector connector;

    @Test
    void testConsumeFileEvent()
    {
        InMemorySource<FileEvent> events = connector.source( "file-event-in");

        FileEvent fileEvent = new FileEvent();

        // Use the send method to send a mock message to the events channel. So, our application will process this message.
        events.send(fileEvent);

        // TODO wait the app to process the event

        // TODO verify the result after processing the event

    }

    @Test
    void testProduceFileEvent()
    {
        InMemorySink<FileEvent> queue = connector.sink( "file-event-out");

        // TODO trigger the application to send the message

        // Assertions.assertEquals( 1, queue.received().size() );
        // FileEvent event = queue.received().get(0).getPayload();

    }

}
