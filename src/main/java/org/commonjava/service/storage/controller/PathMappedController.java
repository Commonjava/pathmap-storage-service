package org.commonjava.service.storage.controller;

import org.apache.commons.io.IOUtils;
import org.commonjava.service.storage.jaxrs.PathMappedFileSystemResult;
import org.commonjava.service.storage.jaxrs.PathMappedFileSystemSetResult;
import org.commonjava.service.storage.jaxrs.PathMappedListResult;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.commonjava.storage.pathmapped.spi.PathDB;
import org.commonjava.storage.pathmapped.spi.PathDB.FileType;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@ApplicationScoped
public class PathMappedController
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    PathMappedFileManager fileManager;

    private static final int DEFAULT_RECURSIVE_LIST_LIMIT = 5000;

    public InputStream openInputStream( String packageType, String type, String name, String path) throws IOException
    {

        String fileSystem = getFileSystem( packageType, type, name );

        return fileManager.openInputStream( fileSystem, path );
    }

    private String getFileSystem( String packageType, String type, String name )
    {
        return packageType + ":" + type + ":" + name;
    }

    public boolean delete( String packageType, String type, String name, String path )
    {
        String fileSystem = getFileSystem( packageType, type, name );
        if ( fileManager.exists( fileSystem, path ) )
        {
            return fileManager.delete( fileSystem, path );
        }
        return false;
    }

    public PathMappedListResult list( String packageType, String type, String name, String path, boolean recursive, String fileType, int limit )
    {

        PathDB.FileType fType = FileType.all;
        if ( isNotBlank( fileType ) )
        {
            fType = FileType.valueOf( fileType );
        }

        String[] list;
        String fileSystem = getFileSystem( packageType, type, name );
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
        return new PathMappedListResult( packageType, type, name, path, list );
    }

    public void create( String packageType, String type, String name, String path, HttpRequest request ) throws IOException
    {
        String fileSystem = getFileSystem( packageType, type, name );

        try (InputStream in = request.getInputStream();
                        OutputStream out = fileManager.openOutputStream( fileSystem, path ))
        {
            IOUtils.copy( in, out );
        }
        catch ( IOException e )
        {
            logger.error( "", e );
            throw e;
        }

    }

    public PathMappedFileSystemSetResult getFileSystemContaining( Collection<String> candidates, String path )
    {
        Set<String> fileSystems = fileManager.getFileSystemContaining( candidates, path );
        return new PathMappedFileSystemSetResult( path, fileSystems );
    }

    public PathMappedFileSystemResult getFileInfo( String packageType, String type, String name, String path )
    {
        String fileSystem = getFileSystem( packageType, type, name );

        String storagePath = fileManager.getFileStoragePath( fileSystem, path );
        long fileLength = fileManager.getFileLength( fileSystem, path );

        PathMappedFileSystemResult result = new PathMappedFileSystemResult( packageType, type, name, path );
        result.setStoragePath( storagePath );
        result.setFileLength( fileLength );

        return result;
    }
}
