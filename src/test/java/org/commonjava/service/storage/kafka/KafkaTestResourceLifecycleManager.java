package org.commonjava.service.storage.kafka;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * Use the in-memory connector to avoid having to use a broker.
 */
public class KafkaTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager
{
    @Override
    public Map<String, String> start()
    {
        Map<String, String> env = new HashMap<>();
        Map<String, String> props1 = InMemoryConnector.switchIncomingChannelsToInMemory("file-event-in");
        Map<String, String> props2 = InMemoryConnector.switchOutgoingChannelsToInMemory("file-event-out");
        env.putAll(props1);
        env.putAll(props2);
        return env;
    }

    @Override
    public void stop()
    {
        InMemoryConnector.clear();
    }
}
