package org.commonjava.service.storage.event;

import org.commonjava.event.file.FileEvent;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FileEventProducer
{

    @Inject
    @Channel("file-event-out")
    Emitter<FileEvent> emitter;

    public void sendFileEventToKafka(FileEvent event) {
        emitter.send( event );
    }

}
