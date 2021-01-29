/**
 * Copyright (C) 2019 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.service.storage.spi;


import org.commonjava.service.storage.model.PathMap;
import org.commonjava.service.storage.model.Reclaim;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface PathDB
{
    enum FileType {
        all, file, dir;
    };

    List<PathMap> list( String fileSystem, String path, FileType fileType );

    List<PathMap> list( String fileSystem, String path, boolean recursive, int limit, FileType fileType );

    long getFileLength( String fileSystem, String path );

    long getFileLastModified( String fileSystem, String path );

    FileType exists( String fileSystem, String path );

    boolean existsFile( String fileSystem, String path );

    void insert( String fileSystem, String path, Date creation, Date expiration, String fileId, long size, String fileStorage, String checksum );

    boolean isDirectory( String fileSystem, String path );

    boolean isFile( String fileSystem, String path );

    boolean delete( String fileSystem, String path );

    String getStorageFile( String fileSystem, String path );

    boolean copy( String fromFileSystem, String fromPath, String toFileSystem, String toPath );

    void makeDirs( String fileSystem, String path );

    List<Reclaim> listOrphanedFiles( int limit );

    default List<Reclaim> listOrphanedFiles() { return listOrphanedFiles( 0 ); }

    void removeFromReclaim( Reclaim reclaim );

    Set<String> getFileSystemContaining( Collection<String> candidates, String path );

    String getFirstFileSystemContaining( List<String> candidates, String path );

    /**
     * Traverse a file system starting from the specified path
     * @param fileSystem
     * @param path starting path, e.g, "/"
     * @param consumer
     * @param limit return no more than limited paths. no limit if limit <= 0
     * @param fileType file, dir, or all
     */
    void traverse( String fileSystem, String path, Consumer<PathMap> consumer, int limit, FileType fileType );
}
