package org.commonjava.service.storage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.commonjava.service.storage.dto.BatchCleanupResult;
import org.commonjava.service.storage.dto.FileInfoObj;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.commonjava.service.storage.util.Utils.getDuration;

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

    public boolean exists( String fileSystem, String path )
    {
        return fileManager.exists( fileSystem, path );
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
        return list;
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
        String storagePath = fileManager.getFileStoragePath( filesystem, path );
        long fileLength = fileManager.getFileLength( filesystem, path );

        FileInfoObj result = new FileInfoObj( filesystem, path );
        result.setStoragePath( storagePath );
        result.setFileLength( fileLength );

        return result;
    }

    public BatchCleanupResult cleanup(String path, Set<String> filesystems )
    {
        Set<String> success = new HashSet<>();
        Set<String> failures = new HashSet<>();
        for ( String fs : filesystems )
        {
            if ( fileManager.delete( fs, path ) )
            {
                success.add( fs );
            }
            else
            {
                failures.add( fs );
            }
        }
        BatchCleanupResult result = new BatchCleanupResult( path );
        result.setSuccess( success );
        result.setFailures( failures );
        return result;
    }
}
