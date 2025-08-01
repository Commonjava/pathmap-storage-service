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
package org.commonjava.service.storage;

import io.quarkus.test.junit.QuarkusTest;
import org.commonjava.service.storage.controller.StorageController;
import org.commonjava.service.storage.dto.BatchCleanupResult;
import org.commonjava.service.storage.dto.FileInfoObj;
import org.commonjava.service.storage.dto.BatchDeleteResult;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StorageControllerIT extends StorageIT
{
    @Inject
    StorageController controller;

    @Test
    public void testGetFileInfo() throws Exception
    {
        FileInfoObj result = controller.getFileInfo( filesystem, PATH );
        assertNotNull( result.getStoragePath() );
        assertNotNull( result.getLastModified() );
        assertNull( result.getExpiration() );
        assertNotEquals( -1, result.getFileLength() );
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
        FileInfoObj result = controller.getFileInfo( filesystem, PATH );
        assertNotEquals( -1, result.getFileLength() );

        Set<String> repos = new HashSet<>();
        repos.add( filesystem );
        HashSet<String> paths = new HashSet<>();
        paths.add(PATH);
        BatchCleanupResult cleanupResult = controller.cleanup( paths, repos );
        String expected = Paths.get(filesystem, PATH).toString();
        assertEquals( expected, String.join( ",", cleanupResult.getSucceeded() ) );

        // After cleanup
        result = controller.getFileInfo( filesystem, PATH );
        assertNull( result );
    }

    /**
     * Test cleanupEmptyFolders for:
     * - Deleting empty folders (should succeed)
     * - Not deleting non-empty folders (should remain)
     * - Recursive parent cleanup (parents deleted if they become empty)
     * - Overlapping folder trees (shared parents handled efficiently)
     * - Correct reporting in BatchDeleteResult (succeeded/failed)
     * - Actual state of the storage after cleanup
     */
    @Test
    public void testCleanupEmptyFolders_recursiveAndOverlapping() throws Exception {
        // Setup: create a nested directory structure
        // Structure:
        // root/
        //   a/
        //     b/   (empty)
        //     c/   (contains file)
        //   d/     (empty)
        //   e/f/g/ (empty)
        String root = "test-root";
        String a = root + "/a";
        String b = a + "/b";
        String c = a + "/c";
        String d = root + "/d";
        String e = root + "/e";
        String f = e + "/f";
        String g = f + "/g";
        String fileInC = c + "/file.txt";

        // Create directories (by writing and deleting a dummy file)
        createEmptyDirectory(filesystem, b);
        createEmptyDirectory(filesystem, c);
        createEmptyDirectory(filesystem, d);
        createEmptyDirectory(filesystem, g);
        // Add a file to c (so c and a are not empty)
        try (OutputStream out = fileManager.openOutputStream(filesystem, fileInC)) {
            out.write("data".getBytes());
        }

        // Sanity check: all directories exist
        assertTrue(fileManager.isDirectory(filesystem, b));
        assertTrue(fileManager.isDirectory(filesystem, c));
        assertTrue(fileManager.isDirectory(filesystem, d));
        assertTrue(fileManager.isDirectory(filesystem, g));
        assertTrue(fileManager.isDirectory(filesystem, f));
        assertTrue(fileManager.isDirectory(filesystem, e));
        assertTrue(fileManager.isDirectory(filesystem, a));
        assertTrue(fileManager.isDirectory(filesystem, root));

        // Call cleanupEmptyFolders on [b, d, g]
        Set<String> toCleanup = new HashSet<>();
        toCleanup.add(b); // should delete b, then a (if a becomes empty)
        toCleanup.add(d); // should delete d
        toCleanup.add(g); // should delete g, f, e (if they become empty)
        BatchDeleteResult result = controller.cleanupEmptyFolders(filesystem, toCleanup);

        // Check results
        // b, d, g, f, e should be deleted (a and root remain because c is not empty)
        assertTrue(result.getSucceeded().contains(b));
        assertTrue(result.getSucceeded().contains(d));
        assertTrue(result.getSucceeded().contains(g));
        assertTrue(result.getSucceeded().contains(f));
        assertTrue(result.getSucceeded().contains(e));
        // a, c, root should not be deleted
        assertFalse(result.getSucceeded().contains(a));
        assertFalse(result.getSucceeded().contains(c));
        assertFalse(result.getSucceeded().contains(root));
        // No failures expected
        assertTrue(result.getFailed().isEmpty());
        // Check actual state
        assertFalse(fileManager.isDirectory(filesystem, b));
        assertFalse(fileManager.isDirectory(filesystem, d));
        assertFalse(fileManager.isDirectory(filesystem, g));
        assertFalse(fileManager.isDirectory(filesystem, f));
        assertFalse(fileManager.isDirectory(filesystem, e));
        assertTrue(fileManager.isDirectory(filesystem, a));
        assertTrue(fileManager.isDirectory(filesystem, c));
        assertTrue(fileManager.isDirectory(filesystem, root));
        // File in c should still exist
        assertTrue(fileManager.exists(filesystem, fileInC));
    }

}
