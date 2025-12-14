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
package org.commonjava.service.storage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Startup;
import org.apache.commons.io.IOUtils;
import org.commonjava.service.storage.config.StorageServiceConfig;
import org.commonjava.service.storage.dto.*;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.storage.pathmapped.model.FileChecksum;
import org.commonjava.storage.pathmapped.model.Filesystem;
import org.commonjava.storage.pathmapped.model.PathMap;
import org.commonjava.storage.pathmapped.spi.PathDB;
import org.commonjava.storage.pathmapped.spi.PathDB.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.commonjava.service.storage.util.Utils.getDuration;
import static org.commonjava.service.storage.util.Utils.sort;
import static org.commonjava.service.storage.util.Utils.depth;
import static org.commonjava.service.storage.util.Utils.getParentPath;
import static org.commonjava.service.storage.util.Utils.getAllCandidates;
import static org.commonjava.service.storage.util.Utils.normalizeFolderPath;
import static org.commonjava.storage.pathmapped.util.PathMapUtils.ROOT_DIR;

@Startup
@ApplicationScoped
public class StorageController
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    PathMappedFileManager fileManager;

    @Inject
    StorageServiceConfig config;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final int DEFAULT_RECURSIVE_LIST_LIMIT = 5000;

    // Cache for filesystems list
    private volatile CachedFilesystems cachedFilesystems;

    public InputStream openInputStream( String fileSystem, String path) throws IOException
    {
        return fileManager.openInputStream( fileSystem, path );
    }

    public boolean exist(String fileSystem, String path )
    {
        return fileManager.exists( fileSystem, path );
    }

    public BatchExistResult exist(final BatchExistRequest request)
    {
        final String filesystem = request.getFilesystem();
        final Set<String> paths = request.getPaths();
        BatchExistResult result = new BatchExistResult();
        result.setFilesystem( filesystem );
        Set<String> missing = new HashSet<>();
        if ( paths != null )
        {
            paths.forEach( p -> {
                boolean exists = fileManager.exists( filesystem, p);
                if (!exists)
                {
                    missing.add(p);
                }
            });
        }
        result.setMissing( missing );
        return result;
    }

    public boolean delete( String fileSystem, String path )
    {
        return fileManager.delete( fileSystem, path );
    }

    public String list(String filesystem, String path) throws Exception {
        return objectMapper.writeValueAsString( fileManager.list(filesystem, path) );
    }

    public String[] list(String fileSystem, String path, boolean recursive, String fileType, int limit ) throws Exception {

        PathDB.FileType fType = FileType.all;
        if ( isNotBlank( fileType ) )
        {
            fType = FileType.valueOf( fileType );
        }

        String[] list;
        if ( recursive )
        {
            int lmt = DEFAULT_RECURSIVE_LIST_LIMIT;
            if ( limit > 0 )
            {
                lmt = limit;
            }
            list = fileManager.list( fileSystem, path, true, lmt, fType );
        }
        else
        {
            list = fileManager.list( fileSystem, path, fType );
        }
        return sort(list);
    }

    /**
     * Write a file to storage.
     * @param filesystem
     * @param path
     * @param inputStream
     * @param timeout a Duration from a ISO8601 standard string as 'PnDTnHnMnS', as in Duration.parse(string).
     *                a simplified format is supported without P or T, such as '3d5h5m10s'.
     * @throws IOException
     */
    public void writeFile(String filesystem, String path, InputStream inputStream, String timeout) throws IOException
    {
        logger.debug("Write file, filesystem: {}, path: {}, timeout: [{}]", filesystem, path, timeout);
        long seconds = 0;
        if ( isNotBlank( timeout ))
        {
            Duration d = getDuration( timeout );
            seconds = d.toSeconds();
        }
        try (OutputStream outputStream = fileManager.openOutputStream(filesystem, path, seconds, TimeUnit.SECONDS) )
        {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    public Collection<String> getFileSystemContaining( Collection<String> candidates, String path ) throws Exception {
        return fileManager.getFileSystemContaining( candidates, path );
    }

    public FileInfoObj getFileInfo(String filesystem, String path )
    {
        PathMap pm = fileManager.getPathMap( filesystem, path );
        if ( pm != null ) {
            FileInfoObj result = new FileInfoObj(filesystem, path);
            result.setStoragePath(pm.getFileStorage());
            result.setFileLength(pm.getSize());
            result.setLastModified(pm.getCreation());
            result.setExpiration(pm.getExpiration());
            return result;
        }
        return null;
    }

    /**
     * Get paths (each includes the filesystem+path) by file's checksum.
     * @return The paths set (because each physical file may link to multiple logical files).
     */
    public Set<String> getFilePathsByChecksum( String checksum )
    {
        final PathDB pathDB = fileManager.getPathDB();
        FileChecksum c = pathDB.getFileChecksum( checksum );
        if ( c != null )
        {
            String fileId = c.getFileId();
            return pathDB.getPathsByFileId( fileId );
        }
        return emptySet();
    }

    public BatchCleanupResult cleanup(Set<String> paths, Set<String> filesystems )
    {
        Set<String> success = new HashSet<>();
        Set<String> failures = new HashSet<>();
        for ( String fs : filesystems )
        {
            for (String path : paths) {
                String mergedPath = Paths.get(fs, path).toString();
                if ( fileManager.delete( fs, path ) )
                {
                    success.add( mergedPath );
                }
                else
                {
                    failures.add( mergedPath );
                }
            }
        }
        BatchCleanupResult result = new BatchCleanupResult();
        result.setSucceeded( success );
        result.setFailed( failures );
        return result;
    }

    public BatchDeleteResult cleanup(Collection<String> paths, String filesystem)
    {
        Set<String> succeeded = new HashSet<>();
        Set<String> failed = new HashSet<>();
        if ( paths != null )
        {
            for (String p : paths)
            {
                if (fileManager.delete(filesystem, p, true))
                {
                    succeeded.add(p);
                }
                else
                {
                    failed.add(p);
                }
            }
        }
        BatchDeleteResult result = new BatchDeleteResult();
        result.setFilesystem( filesystem );
        result.setSucceeded( succeeded );
        result.setFailed( failed );
        return result;
    }

    public BatchDeleteResult purgeFilesystem(String filesystem)
    {
        // list and delete all dir/files
        String[] files = fileManager.list( filesystem, ROOT_DIR, true, 0, FileType.all );
        BatchDeleteResult ret = cleanup(Arrays.asList(files), filesystem);
        // also remove the statistics entry
        Filesystem statistics = fileManager.getFilesystem(filesystem);
        if ( statistics != null )
        {
            fileManager.purgeFilesystem( statistics );
        }
        invalidateFilesystemsCache();
        return ret;
    }

    public Collection<String> getFilesystems()
    {
        CachedFilesystems cached = cachedFilesystems;
        long now = System.currentTimeMillis();
        
        // Check if cache is valid
        if ( cached != null && ( now - cached.timestamp ) < TimeUnit.SECONDS.toMillis( config.filesystemsCacheTtlSeconds() ) )
        {
            logger.debug( "Returning cached filesystems list (age: {}ms)", now - cached.timestamp );
            return cached.filesystems;
        }
        
        // Cache miss or expired - fetch from database
        logger.debug( "Cache miss or expired, fetching filesystems from database" );
        Collection<? extends Filesystem> filesystems = fileManager.getFilesystems();
        Collection<String> result;
        if ( filesystems != null ) {
            result = filesystems.stream().map(filesystem -> filesystem.getFilesystem()).sorted().collect(Collectors.toList());
        } else {
            result = emptyList();
        }
        
        // Update cache
        cachedFilesystems = new CachedFilesystems( result, now );
        logger.debug( "Cached filesystems list (size: {})", result.size() );
        return result;
    }

    public Collection<? extends Filesystem> getEmptyFilesystems()
    {
        return fileManager.getFilesystems().stream()
                .filter(filesystem -> filesystem.getFileCount() == 0)
                .collect(Collectors.toList());
    }

    public void purgeEmptyFilesystems()
    {
        Collection<? extends Filesystem> ret = getEmptyFilesystems();
        ret.forEach( filesystem -> fileManager.purgeFilesystem( filesystem ));
        invalidateFilesystemsCache();
    }

    /**
     * Cleans up (deletes) the given empty folders and, if possible, their parent folders up to the root.
     * <p>
     * Optimization details:
     * <ul>
     *   <li>Collects all input folders and their ancestors as candidates for deletion.</li>
     *   <li>Sorts candidates by depth (deepest first) to ensure children are processed before parents.</li>
     *   <li>Attempts to delete each folder only once (tracked in the processed set).</li>
     *   <li>If a folder is not empty, marks it and all its ancestors as processed, since their parents must also be non-empty.</li>
     *   <li>Tracks succeeded and failed deletions in the result object.</li>
     *   <li>Returns a BatchDeleteResult with all attempted deletions for client inspection.</li>
     * </ul>
     * This approach avoids redundant deletion attempts and is efficient for overlapping directory trees.
     */
    public BatchDeleteResult cleanupEmptyFolders(String filesystem, Collection<String> paths) {
        Set<String> allCandidates = getAllCandidates(paths);
        // Sort by depth, deepest first
        List<String> sortedCandidates = new ArrayList<>(allCandidates);
        sortedCandidates.sort((a, b) -> Integer.compare(depth(b), depth(a)));
        logger.debug("Sorted candidate folders for cleanup (deepest first): {}", sortedCandidates);

        Set<String> succeeded = new HashSet<>();
        Set<String> failed = new HashSet<>();
        Set<String> processed = new HashSet<>();
        for (String folder : sortedCandidates) {
            if (processed.contains(folder)) {
                continue;
            }
            processed.add(folder);
            try {
                if (!fileManager.isDirectory(filesystem, folder)) {
                    logger.debug("Path is not a directory or does not exist, skipping: {} in filesystem: {}", folder, filesystem);
                    continue;
                }
                String[] contents = fileManager.list(filesystem, folder);
                if (contents == null || contents.length == 0) {
                    boolean deleted = fileManager.delete(filesystem, normalizeFolderPath(folder));
                    if (deleted) {
                        succeeded.add(folder);
                        logger.debug("Folder deleted: {} in filesystem: {}", folder, filesystem);
                    } else {
                        failed.add(folder);
                    }
                } else {
                    logger.debug("Folder not empty, skipping cleanup: {} in filesystem: {}, contents: {}", folder, filesystem, Arrays.toString(contents));
                    // Mark this folder and all its ancestors as processed
                    markAncestorsProcessed(folder, processed);
                }
            } catch (Exception e) {
                logger.warn("Failed to clean up folder: {} in filesystem: {}. Error: {}", folder, filesystem, e.getMessage());
                failed.add(folder);
                markAncestorsProcessed(folder, processed);
            }
        }
        logger.info("Cleanup empty folders result: succeeded={}, failed={}", succeeded, failed);
        BatchDeleteResult result = new BatchDeleteResult();
        result.setFilesystem(filesystem);
        result.setSucceeded(succeeded);
        result.setFailed(failed);
        return result;
    }

    // Mark the given folder and all its ancestors as processed
    private void markAncestorsProcessed(String folder, Set<String> processed) {
        String current = folder;
        while (current != null && !current.isEmpty() && !current.equals("/")) {
            processed.add(current);
            current = getParentPath(current);
        }
    }

    /**
     * By default, the pathmap storage will override existing paths. Here we must check:
     * 1. all paths exist in source
     * 2. if not allow override, all paths must not exist in target
     */
    public FileCopyResult copy(FileCopyRequest request)
    {
        if ( isBlank(request.getSourceFilesystem()) || isBlank(request.getTargetFilesystem() ) )
        {
            return new FileCopyResult( false, "source or target filesystem null" );
        }

        if ( request.getPaths() == null || request.getPaths().isEmpty() )
        {
            return new FileCopyResult( false, "paths null" );
        }

        // Check paths on source filesystem
        Set<String> missing = new HashSet<>();
        request.getPaths().forEach( p -> {
            boolean exist = fileManager.exists( request.getSourceFilesystem(), p );
            if ( !exist )
            {
                missing.add(p);
            }
        });
        if ( !missing.isEmpty() )
        {
            return new FileCopyResult( false, "paths missing from source: " + missing );
        }

        // if not allow override, paths must not exist in target
        Set<String> existing = new HashSet<>();
        request.getPaths().forEach( p -> {
            boolean exist = fileManager.exists( request.getTargetFilesystem(), p );
            if ( exist )
            {
                existing.add(p);
            }
        });
        logger.debug( "Found {} existing paths in target {}", existing.size(), request.getTargetFilesystem() );
        if ( request.isFailWhenExists() && !existing.isEmpty() )
        {
            return new FileCopyResult( false, existing.size() +
                    " paths exist in target (failWhenExists: true): " + existing );
        }

        Set<String> skipped = new HashSet<>();
        Set<String> completed = new HashSet<>();
        final int count = request.getPaths().size() - existing.size();
        logger.debug( "Copying {} paths from {} to {}", count, request.getSourceFilesystem(),
                request.getTargetFilesystem() );

        final AtomicInteger copied = new AtomicInteger();

        // Update creation (current time) and expiration for copied file
        final Date current = new Date();
        final Date expiration;
        if ( request.getTimeoutSeconds() > 0 )
        {
            expiration = new Date( current.getTime() + TimeUnit.SECONDS.toMillis(request.getTimeoutSeconds()) );
        }
        else
        {
            expiration = null; // never expire
        }

        request.getPaths().forEach( p -> {
            if ( existing.contains( p ) )
            {
                skipped.add(p);
            }
            else
            {
                fileManager.copy(request.getSourceFilesystem(), p, request.getTargetFilesystem(), p, current, expiration);
                completed.add(p);
                logger.debug( "Copied ({}) {}", copied.incrementAndGet(), p );
            }
        });

        return new FileCopyResult( true, completed, skipped );
    }

    /**
     * Invalidates the filesystems cache. Should be called whenever filesystems are added or removed.
     */
    private void invalidateFilesystemsCache()
    {
        logger.debug( "Invalidating filesystems cache" );
        cachedFilesystems = null;
    }

    /**
     * Simple cache entry for filesystems list.
     */
    private static class CachedFilesystems
    {
        final Collection<String> filesystems;
        final long timestamp;

        CachedFilesystems( Collection<String> filesystems, long timestamp )
        {
            this.filesystems = filesystems;
            this.timestamp = timestamp;
        }
    }
}
