package org.commonjava.service.storage.controller;

import org.commonjava.storage.pathmapped.core.PathMappedFileManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class PathMappedController
{

    @Inject
    PathMappedFileManager fileManager;

    public InputStream openInputStream( String packageType, String type, String name, String path) throws IOException
    {

         String fileSystem = getFileSystem( packageType, type, name );

        return fileManager.openInputStream( fileSystem, path );
    }

    private String getFileSystem( String packageType, String type, String name )
    {
        return packageType + ":" + type + ":" + name;
    }

}
