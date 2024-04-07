/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.service.storage.core;

import org.commonjava.service.storage.config.CassandraConfig;
import org.commonjava.service.storage.config.StorageServiceConfig;
import org.commonjava.storage.pathmapped.config.DefaultPathMappedStorageConfig;
import org.commonjava.storage.pathmapped.config.PathMappedStorageConfig;
import org.commonjava.storage.pathmapped.core.FileBasedPhysicalStore;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.storage.pathmapped.core.S3PhysicalStore;
import org.commonjava.storage.pathmapped.pathdb.datastax.CassandraPathDB;
import org.commonjava.storage.pathmapped.spi.PathDB;
import org.commonjava.storage.pathmapped.spi.PhysicalStore;
import software.amazon.awssdk.services.s3.S3Client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
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

    @Inject
    S3Client s3Client;

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
        PhysicalStore physicalStore;
        String storageType = storageConfig.type();

        if ( StorageServiceConfig.STORAGE_S3.equals( storageType ) )
        {
            physicalStore = new S3PhysicalStore( s3Client, storageConfig.bucketName() );
        }
        else
        {
            physicalStore = new FileBasedPhysicalStore( storageConfig.baseDir() );
        }

        return new PathMappedFileManager( config, pathDB, physicalStore );
    }

}
