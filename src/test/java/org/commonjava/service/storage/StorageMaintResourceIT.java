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
import org.commonjava.service.storage.dto.BatchDeleteRequest;
import org.commonjava.service.storage.dto.BatchDeleteResult;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class StorageMaintResourceIT extends StorageIT {

    @Override
    protected boolean isPrepareFile() {
        return false;
    }

    /**
     * Integration test for the cleanupEmptyFolders REST endpoint.
     * <p>
     * Folder structure used in this test:
     * <pre>
     * test-root-http/
     * ├── a/
     * │   ├── b/   (not empty, contains file.txt)
     * │   └── file.txt (so a is not empty)
     * └── d/     (empty)
     * </pre>
     * <ul>
     *   <li>Sets up a nested directory structure with both empty and non-empty folders.</li>
     *   <li>Sends a DELETE request to /api/storage/maint/folders/empty with a batch of folders.</li>
     *   <li>Asserts that only empty folders are deleted and non-empty folders remain.</li>
     *   <li>Verifies the HTTP response and the BatchDeleteResult content.</li>
     * </ul>
     */
    @Test
    public void testCleanupEmptyFolders_HttpLevel() {
        // Setup: create a nested directory structure as in the controller test
        String root = "test-root-http";
        String a = root + "/a";
        String b = a + "/b";
        String d = root + "/d";
        String fileInB = b + "/file.txt";
        String fileInA = a + "/file.txt";

        // Create directories (by writing and deleting a dummy file)
        try {
            createEmptyDirectory(filesystem, b);
            createEmptyDirectory(filesystem, d);
            // Add a file to b (so b is not empty)
            try (java.io.OutputStream out = fileManager.openOutputStream(filesystem, fileInB)) {
                out.write("data".getBytes());
            }
            // Add a file to a (so a is not empty)
            try (java.io.OutputStream out = fileManager.openOutputStream(filesystem, fileInA)) {
                out.write("data".getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Prepare request: try to clean up b and d
        BatchDeleteRequest request = new BatchDeleteRequest();
        request.setFilesystem(filesystem);
        Set<String> paths = new HashSet<>(Arrays.asList(b, d));
        request.setPaths(paths);

        // Send DELETE request to the API
        BatchDeleteResult result =
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
            .when()
                .delete("/api/storage/maint/folders/empty")
            .then()
                .statusCode(200)
                .extract().as(BatchDeleteResult.class);

        // d should be deleted (empty), b should not (not empty)
        assertThat(result.getSucceeded(), hasItem(d));
        assertThat(result.getSucceeded(), not(hasItem(b)));
        assertThat(result.getFailed(), not(hasItem(d)));
    }
} 