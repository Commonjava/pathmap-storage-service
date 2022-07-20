package org.commonjava.service.storage;

import io.quarkus.test.junit.QuarkusTest;
import org.commonjava.service.storage.controller.StorageController;
import org.commonjava.service.storage.dto.BatchCleanupResult;
import org.commonjava.service.storage.dto.FileInfoObj;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class StorageControllerTest extends StorageTest
{
    @Inject
    StorageController controller;

    @Test
    public void testGetFileInfo() throws Exception
    {
        FileInfoObj result = controller.getFileInfo( filesystem, PATH);
        Assertions.assertNotNull( result.getStoragePath() );
        Assertions.assertNotEquals( -1, result.getFileLength() );
    }

    @Test
    public void testListFile() throws Exception {
        String[] result = controller.list(filesystem, DIR, true, "all", 500);
        assertEquals(FILE, String.join( ",", result ) );
    }

    @Test
    public void testGetFileSystemContaining() throws Exception {
        Collection<String> result = controller.getFileSystemContaining(
                Arrays.asList("maven:remote:central", "maven:group:public"),
                PATH);
        assertEquals( "maven:remote:central", String.join( ",", result ) );
    }

    @Test
    public void testCleanup()
    {
        // Before cleanup
        FileInfoObj result = controller.getFileInfo( filesystem, PATH);
        Assertions.assertNotEquals( -1, result.getFileLength() );

        Set<String> repos = new HashSet<>();
        repos.add( filesystem );
        BatchCleanupResult cleanupResult = controller.cleanup(PATH, repos );
        assertEquals( filesystem, String.join( ",", cleanupResult.getSuccess() ) );

        // After cleanup
        result = controller.getFileInfo( filesystem, PATH);
        assertEquals( -1, result.getFileLength() );
    }

}

