package org.commonjava.service.storage.core;

import org.commonjava.service.storage.config.CassandraConfiguration;
import org.commonjava.service.storage.config.IndyPathMappedConfiguration;
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
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.PROP_CASSANDRA_HOST;
import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.PROP_CASSANDRA_KEYSPACE;
import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.PROP_CASSANDRA_PASS;
import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.PROP_CASSANDRA_PORT;
import static org.commonjava.storage.pathmapped.pathdb.datastax.util.CassandraPathDBUtils.PROP_CASSANDRA_USER;

@ApplicationScoped
public class FileManagerProducer
{

    @Inject
    CassandraConfiguration cassandraConfig;

    @Inject
    IndyPathMappedConfiguration storageConfig;

    @Produces
    public PathMappedFileManager getFileManager()
    {

        Map<String, Object> props = new HashMap<>();
        props.put( PROP_CASSANDRA_HOST, cassandraConfig.getCassandraHost() );
        props.put( PROP_CASSANDRA_PORT, cassandraConfig.getCassandraPort() );
        props.put( PROP_CASSANDRA_KEYSPACE, cassandraConfig.getKeyspace() );
        props.put( PROP_CASSANDRA_USER, cassandraConfig.getCassandraUser() );
        props.put( PROP_CASSANDRA_PASS, cassandraConfig.getCassandraPass() );

        PathMappedStorageConfig config = new DefaultPathMappedStorageConfig( props );

        File baseDir = storageConfig.getStorageRootDirectory();

        PathDB pathDB = new CassandraPathDB( config );

        PhysicalStore physicalStore = new FileBasedPhysicalStore( baseDir );

        PathMappedFileManager fileManager = new PathMappedFileManager( config, pathDB, physicalStore );

        return fileManager;
    }

}
