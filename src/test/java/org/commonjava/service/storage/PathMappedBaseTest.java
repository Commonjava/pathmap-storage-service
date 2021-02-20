package org.commonjava.service.storage;

import io.quarkus.test.common.http.TestHTTPResource;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.net.URL;

public class PathMappedBaseTest
{

    protected final String PACKAGE_TYPE = "maven";
    protected final String TYPE = "remote";
    protected final String NAME = "central";
    protected final String PATH = "io/quarkus/quarkus-junit5/quarkus-junit5-1.12.0.Final.jar";
    protected final String DIR = "io/quarkus/quarkus-junit5";
    protected final String FILE_NAME = "quarkus-junit5-1.12.0.Final.jar";

    @TestHTTPResource( FILE_NAME )
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

}
