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
package org.commonjava.service.storage.config;

import io.quarkus.runtime.Startup;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;

@Startup
@ConfigMapping( prefix = "storage" )
@ApplicationScoped
public interface StorageServiceConfig
{
    String STORAGE_S3 = "s3";

    String STORAGE_NFS = "nfs";

    @WithName( "baseDir" )
    File baseDir();

    @WithName( "readonly" )
    boolean readonly();

    @WithName( "removableFilesystemPattern" )
    String removableFilesystemPattern();

    @WithName( "physicalFileExistenceCheck" )
    @WithDefault("false")
    boolean physicalFileExistenceCheck();

    @WithName( "gcBatchSize" )
    @WithDefault("100")
    int gcBatchSize();

    // value <= 0 disables gc effectively
    @WithName( "gcIntervalInMinutes" )
    @WithDefault("60")
    int gcIntervalInMinutes();

    @WithName( "type" )
    @WithDefault( STORAGE_NFS )
    String type();

    @WithName( "bucket.name" )
    @WithDefault( "test" )
    String bucketName();
}
