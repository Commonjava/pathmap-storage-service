package org.commonjava.service.storage;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class PathMappedResourceTest
                extends PathMappedBaseTest
{

    @Inject
    PathMappedFileManager fileManager;

    @BeforeEach
    public void start() throws Exception
    {
        String fileSystem = PACKAGE_TYPE + ":" + TYPE + ":" + NAME;

        try (InputStream in = url.openStream(); OutputStream out = fileManager.openOutputStream( fileSystem, PATH ))
        {
            IOUtils.copy( in, out );
        }
        catch ( IOException e )
        {
            throw e;
        }
    }

    @Test
    public void testGetFile()
    {
        given().pathParam( "packageType", PACKAGE_TYPE )
               .pathParam( "type", TYPE )
               .pathParam( "name", NAME )
               .pathParam( "path", PATH )
               .when()
               .get( "/api/pathmapped/content/{packageType}/{type}/{name}/{path}" )
               .then()
               .statusCode( 200 );
    }

    @Test
    public void testGetFileInfo()
    {
        Response response = given().pathParam( "packageType", PACKAGE_TYPE )
                                   .pathParam( "type", TYPE )
                                   .pathParam( "name", NAME )
                                   .pathParam( "path", PATH )
                                   .when()
                                   .get( "/api/pathmapped/filesystem/content/{packageType}/{type}/{name}/{path}/info" )
                                   .then()
                                   .extract()
                                   .response();

        Assertions.assertEquals( 200, response.statusCode() );
        Assertions.assertNotNull( response.jsonPath().getString( "storagePath" ) );
        Assertions.assertNotEquals( -1, response.jsonPath().getLong( "fileLength" ) );
    }

    @Test
    public void testList()
    {
        Response response = given().pathParam( "packageType", PACKAGE_TYPE )
                                   .pathParam( "type", TYPE )
                                   .pathParam( "name", NAME )
                                   .pathParam( "path", DIR )
                                   .queryParam( "recursive", true )
                                   .when()
                                   .get( "/api/pathmapped/browse/{packageType}/{type}/{name}/{path}" )
                                   .then()
                                   .extract()
                                   .response();

        Assertions.assertEquals( 200, response.statusCode() );
        Assertions.assertEquals( "quarkus-junit5-1.12.0.Final.jar",
                                 String.join( ",", response.jsonPath().getList( "list" ) ) );
    }

}
