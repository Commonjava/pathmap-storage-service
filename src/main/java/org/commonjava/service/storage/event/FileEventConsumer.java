package org.commonjava.service.storage.event;

import org.commonjava.event.file.FileEvent;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileEventConsumer
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Incoming("file-event-in")
    public void receive( FileEvent event) {
        logger.info("Got an event: {}", event);
    }
}
