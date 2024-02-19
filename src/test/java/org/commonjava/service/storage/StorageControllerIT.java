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
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

}
