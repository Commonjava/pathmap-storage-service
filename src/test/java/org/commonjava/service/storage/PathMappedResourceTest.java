package org.commonjava.service.storage;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.commonjava.storage.pathmapped.core.PathMappedFileManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

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
    public void testGetNonExistingFile()
    {
        given().pathParam( "packageType", PACKAGE_TYPE )
               .pathParam( "type", TYPE )
               .pathParam( "name", NAME )
               .pathParam( "path", "non-existing-path" )
               .when()
               .get( "/api/pathmapped/content/{packageType}/{type}/{name}/{path}" )
               .then()
               .statusCode( 404 );
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

    @Test
    public void testListRoot()
    {
        Response response = given().pathParam( "packageType", PACKAGE_TYPE )
                                   .pathParam( "type", TYPE )
                                   .pathParam( "name", NAME )
                                   .queryParam( "recursive", true )
                                   .when()
                                   .get( "/api/pathmapped/browse/{packageType}/{type}/{name}" )
                                   .then()
                                   .extract()
                                   .response();

        List<String> expectedList = Arrays.asList( "io/", "io/quarkus/", "io/quarkus/quarkus-junit5/",
                                                   "io/quarkus/quarkus-junit5/quarkus-junit5-1.12.0.Final.jar" );

        Assertions.assertEquals( 200, response.statusCode() );
        Assertions.assertEquals( String.join( ",", expectedList ),
                                 String.join( ",", response.jsonPath().getList( "list" ) ) );
    }

    @Test
    public void testCleanup()
    {
        JsonObject request = new JsonObject();
        request.put( "path", PATH );
        request.put( "repositories",
                     Arrays.asList( "maven:remote:central", "maven:hosted:pnc-builds", "maven:group:public" ) );

        Response response = given().contentType( ContentType.JSON )
                                   .body( request.toString() )
                                   .post( "/api/pathmapped/filesystem/cleanup" )
                                   .then()
                                   .extract()
                                   .response();

        Assertions.assertEquals( 200, response.statusCode() );

    }

    @Test
    public void testFileSystemContaining()
    {
        JsonObject request = new JsonObject();
        request.put( "candidates",
                     Arrays.asList( "maven:remote:central", "maven:hosted:pnc-builds", "maven:group:public" ) );

        Response response = given().contentType( ContentType.JSON )
                                   .body( request.toString() )
                                   .pathParam( "path", PATH )
                                   .post( "/api/pathmapped/filesystem/containing/{path}" )
                                   .then()
                                   .extract()
                                   .response();

        Assertions.assertEquals( 200, response.statusCode() );
        Assertions.assertEquals( "maven:remote:central",
                                 String.join( ",", response.jsonPath().getList( "fileSystems" ) ) );
    }

}
