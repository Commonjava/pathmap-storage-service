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

import org.commonjava.storage.pathmapped.core.FileBasedPhysicalStore;
import org.commonjava.storage.pathmapped.spi.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Treat legacy package folders (generic-http, maven, npm) as read-only.
 * Reads are allowed, while writes target the newer hashed layout.
 * Operations that would delete legacy paths are skipped.
 */
public class LegacyReadonlyPhysicalStore extends FileBasedPhysicalStore
{
    public static final String PKG_TYPE_MAVEN = "maven";

    public static final String PKG_TYPE_NPM = "npm";

    public static final String PKG_TYPE_GENERIC_HTTP = "generic-http";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public LegacyReadonlyPhysicalStore( File baseDir )
    {
        super( baseDir );
    }

    @Override
    public boolean delete( FileInfo fileInfo )
    {
        String fileStorage = fileInfo.getFileStorage();
        if ( isLegacyFile( fileStorage ) )
        {
            logger.debug( "Skip read-only legacy file: {}", fileStorage );
            return true;
        }
        return super.delete( fileInfo );
    }

    //Legacy folders: generic-http, maven, npm
    private boolean isLegacyFile( String fileStorage )
    {
        return fileStorage != null && ( fileStorage.startsWith( PKG_TYPE_MAVEN ) || fileStorage.startsWith(
                        PKG_TYPE_GENERIC_HTTP ) || fileStorage.startsWith( PKG_TYPE_NPM ) );
    }
}
