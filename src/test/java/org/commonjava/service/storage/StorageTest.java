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

import io.quarkus.test.common.http.TestHTTPResource;
import org.apache.commons.io.IOUtils;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public abstract class StorageTest
{
    protected final String PATH = "io/quarkus/quarkus-junit5/quarkus-junit5-1.12.0.Final.jar";
    protected final String DIR = "io/quarkus/quarkus-junit5";
    protected final String FILE = "quarkus-junit5-1.12.0.Final.jar";

    protected final String filesystem = "maven:remote:central";

    @Inject
    PathMappedFileManager fileManager;

    @TestHTTPResource(FILE)
    URL url;

    @BeforeAll
    public static void init() throws Exception
    {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }

    @AfterAll
    public static void stop() throws Exception
    {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @BeforeEach
    public void start() throws Exception
    {
        if ( isPrepareFile() ) {
            try (InputStream in = url.openStream()) {
                 prepareFile( in, filesystem, PATH );
            }
        }
    }

    protected void prepareFile( InputStream in, String filesystem, String path ) throws Exception
    {
        try (OutputStream out = fileManager.openOutputStream(filesystem, path)) {
            IOUtils.copy(in, out);
        }
    }

    /**
     * Override this if your test case don't need to prepare the PATH
     * @return
     */
    protected boolean isPrepareFile()
    {
        return true;
    }
}
