package org.commonjava.service.storage;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.commonjava.service.storage.controller.PathMappedController;
import org.commonjava.service.storage.jaxrs.PathMappedCleanupResult;
import org.commonjava.service.storage.jaxrs.PathMappedFileSystemResult;
import org.commonjava.service.storage.jaxrs.PathMappedFileSystemSetResult;
import org.commonjava.service.storage.jaxrs.PathMappedListResult;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@QuarkusTest
public class PathMappedControllerTest extends PathMappedBaseTest
{

    @Inject
    PathMappedController controller;

    @Inject
    PathMappedFileManager fileManager;

    @BeforeEach
    public void start() throws Exception
    {
        String fileSystem = PACKAGE_TYPE + ":" + TYPE + ":" + NAME;

        try (InputStream in = url.openStream();
                        OutputStream out = fileManager.openOutputStream( fileSystem, PATH ))
        {
            IOUtils.copy( in, out );
        }
        catch ( IOException e )
        {
            throw e;
        }
    }

    @Test
    public void testGetFileInfo() throws Exception
    {

        PathMappedFileSystemResult result =
                        controller.getFileInfo( PACKAGE_TYPE, TYPE, NAME, PATH );
        Assertions.assertNotNull( result.getStoragePath() );
        Assertions.assertNotEquals( -1, result.getFileLength() );
    }

    @Test
    public void testListFile()
    {
        PathMappedListResult result = controller.list( PACKAGE_TYPE, TYPE, NAME, DIR, true, "all", 500 );
        Assertions.assertEquals( FILE_NAME, String.join( ",", result.getList() ) );
    }

    @Test
    public void testGetFileSystemContaining()
    {
        PathMappedFileSystemSetResult result = controller.getFileSystemContaining(
                        Arrays.asList( "maven:remote:central", "maven:hosted:pnc-builds", "maven:group:public" ),
                        PATH );
        Assertions.assertEquals( "maven:remote:central", String.join( ",", result.getFileSystems() ) );
    }

    @Test
    public void testCleanup()
    {
        String fileSystem = PACKAGE_TYPE + ":" + TYPE + ":" + NAME;

        // Before cleanup
        PathMappedFileSystemResult result =
                        controller.getFileInfo( PACKAGE_TYPE, TYPE, NAME, PATH );
        Assertions.assertNotEquals( -1, result.getFileLength() );

        Set<String> repos = new HashSet<>();
        repos.add( fileSystem );
        PathMappedCleanupResult cleanupResultresult = controller.cleanup( PATH, repos );
        Assertions.assertEquals( fileSystem, String.join( ",", cleanupResultresult.getSuccess() ) );

        // After cleanup
        result = controller.getFileInfo( PACKAGE_TYPE, TYPE, NAME, PATH );
        Assertions.assertEquals( -1, result.getFileLength() );
    }


}

