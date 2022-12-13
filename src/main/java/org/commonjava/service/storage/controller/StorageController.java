package org.commonjava.service.storage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.commonjava.service.storage.dto.*;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.storage.pathmapped.model.Filesystem;
import org.commonjava.storage.pathmapped.model.PathMap;
import org.commonjava.storage.pathmapped.spi.PathDB;
import org.commonjava.storage.pathmapped.spi.PathDB.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.commonjava.service.storage.util.Utils.getDuration;
import static org.commonjava.service.storage.util.Utils.sort;

@ApplicationScoped
public class StorageController
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    PathMappedFileManager fileManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final int DEFAULT_RECURSIVE_LIST_LIMIT = 5000;

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

    public BatchCleanupResult cleanup(Set<String> paths, Set<String> filesystems )
    {
        Set<String> success = new HashSet<>();
        Set<String> failures = new HashSet<>();
        for ( String fs : filesystems )
        {
            for (String path : paths) {
                if ( fileManager.delete( fs, path ) )
                {
                    success.add( fs + ":" + path );
                }
                else
                {
                    failures.add( fs + ":" + path );
                }
            }
        }
        BatchCleanupResult result = new BatchCleanupResult();
        result.setSucceeded( success );
        result.setFailed( failures );
        return result;
    }

    public BatchDeleteResult cleanup(Set<String> paths, String filesystem) {
        Set<String> succeeded = new HashSet<>();
        Set<String> failed = new HashSet<>();
        for ( String p : paths )
        {
            if ( fileManager.delete( filesystem, p ) )
            {
                succeeded.add( p );
            }
            else
            {
                failed.add( p );
            }
        }
        BatchDeleteResult result = new BatchDeleteResult();
        result.setFilesystem( filesystem );
        result.setSucceeded( succeeded );
        result.setFailed( failed );
        return result;
    }

    public Collection<String> getFilesystems()
    {
        Collection<? extends Filesystem> filesystems = fileManager.getFilesystems();
        if ( filesystems != null ) {
            return filesystems.stream().map(filesystem -> filesystem.getFilesystem()).sorted().collect(Collectors.toList());
        }
        return emptyList();
    }

    /**
     * By default, the pathmap storage will override existing paths. Here we must check:
     * 1. all paths exist in source
     * 2. if not allow override, all paths must not exist in target
     */
    public FileCopyResult copy(FileCopyRequest request) {
        if ( isBlank(request.getSourceFilesystem()) || isBlank(request.getTargetFilesystem() ) )
        {
            return new FileCopyResult( false, "source or target filesystem null" );
        }

        if ( request.getPaths() == null || request.getPaths().isEmpty() )
        {
            return new FileCopyResult( false, "paths null" );
        }

        // check paths existence
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

        // if not allow override, all paths must not exist in target
        Set<String> existing = new HashSet<>();
        if ( request.isFailWhenExists() )
        {
            request.getPaths().forEach( p -> {
                boolean exist = fileManager.exists( request.getTargetFilesystem(), p );
                if ( exist )
                {
                    existing.add(p);
                }
            });
        }
        if ( !existing.isEmpty() )
        {
            return new FileCopyResult( false, "paths existing in target: " + existing );
        }

        Set<String> skipped = new HashSet<>();
        Set<String> completed = new HashSet<>();
        // all check passes
        request.getPaths().forEach( p -> {
            boolean exist = fileManager.exists( request.getTargetFilesystem(), p );
            if ( exist )
            {
                skipped.add(p);
            }
            else
            {
                fileManager.copy(request.getSourceFilesystem(), p, request.getTargetFilesystem(), p);
                completed.add(p);
            }
        });

        return new FileCopyResult( true, completed, skipped );
    }

}
