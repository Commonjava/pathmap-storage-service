package org.commonjava.service.storage.event;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class FileEventDeserializer extends ObjectMapperDeserializer<FileEvent>
{
    public FileEventDeserializer( )
    {
        super( FileEvent.class );
    }
}
