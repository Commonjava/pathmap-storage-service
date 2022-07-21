package org.commonjava.service.storage.core;

import org.commonjava.service.storage.config.CassandraConfig;
import org.commonjava.service.storage.config.StorageServiceConfig;
import org.commonjava.storage.pathmapped.config.DefaultPathMappedStorageConfig;
import org.commonjava.storage.pathmapped.config.PathMappedStorageConfig;
import org.commonjava.storage.pathmapped.core.FileBasedPhysicalStore;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.storage.pathmapped.pathdb.datastax.CassandraPathDB;
import org.commonjava.storage.pathmapped.spi.PathDB;
import org.commonjava.storage.pathmapped.spi.PhysicalStore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.*;

@ApplicationScoped
public class FileManagerProducer
{
    @Inject
    CassandraConfig cassandraConfig;

    @Inject
    StorageServiceConfig storageConfig;

    @Produces
    public PathMappedFileManager getFileManager()
    {
        Map<String, Object> props = new HashMap<>();
        props.put( PROP_CASSANDRA_HOST, cassandraConfig.host() );
        props.put( PROP_CASSANDRA_PORT, cassandraConfig.port() );
        props.put( PROP_CASSANDRA_KEYSPACE, cassandraConfig.keyspace() );
        props.put( PROP_CASSANDRA_USER, cassandraConfig.user() );
        props.put( PROP_CASSANDRA_PASS, cassandraConfig.pass() );

        PathMappedStorageConfig config = new DefaultPathMappedStorageConfig( props );

        PathDB pathDB = new CassandraPathDB( config );
        PhysicalStore physicalStore = new FileBasedPhysicalStore( storageConfig.baseDir() );

        PathMappedFileManager fileManager = new PathMappedFileManager( config, pathDB, physicalStore );
        return fileManager;
    }

}
