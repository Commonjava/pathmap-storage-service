package org.commonjava.service.storage;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.commonjava.service.storage.jaxrs.StorageResource.API_BASE;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StorageResourceTest
                extends StorageTest
{
    @Test
    public void testGetFile()
    {
        given().pathParam( "filesystem", filesystem )
               .pathParam( "path", PATH)
               .when()
               .get( API_BASE + "/content/{filesystem}/{path}" )
               .then()
               .statusCode( 200 );
    }

    @Test
    public void testGetNonExistingFile()
    {
        given().pathParam( "filesystem", filesystem )
               .pathParam( "path", "non-existing-path" )
               .when()
               .get( API_BASE + "/content/{filesystem}/{path}" )
               .then()
               .statusCode( 404 );
    }

    @Test
    public void testGetFileInfo()
    {
        Response response = given().pathParam( "filesystem", filesystem )
                                   .pathParam( "path", PATH)
                                   .when()
                                   .get( API_BASE + "/info/{filesystem}/{path}" )
                                   .then()
                                   .extract()
                                   .response();

        assertEquals( 200, response.statusCode() );
        assertNotNull( response.jsonPath().getString( "storagePath" ) );
        assertNotEquals( -1, response.jsonPath().getLong( "fileLength" ) );
    }

    @Test
    public void testList()
    {
        Response response = given().pathParam( "filesystem", filesystem )
                                   .pathParam( "path", DIR)
                                   .queryParam( "recursive", true )
                                   .when()
                                   .get( API_BASE + "/browse/{filesystem}/{path}" )
                                   .then()
                                   .extract()
                                   .response();

        assertEquals( 200, response.statusCode() );
        assertEquals( "quarkus-junit5-1.12.0.Final.jar",
                                 String.join( ",", response.jsonPath().getList( "" ) ) );
    }

    @Test
    public void testListRoot()
    {
        Response response = given().pathParam( "filesystem", filesystem )
                                   .queryParam( "recursive", true )
                                   .when()
                                   .get( API_BASE + "/browse/{filesystem}" )
                                   .then()
                                   .extract()
                                   .response();

        List<String> expectedList = Arrays.asList( "io/", "io/quarkus/", "io/quarkus/quarkus-junit5/",
                                                   "io/quarkus/quarkus-junit5/quarkus-junit5-1.12.0.Final.jar" );

        assertEquals( 200, response.statusCode() );
        assertEquals( String.join( ",", expectedList ),
                                 String.join( ",", response.jsonPath().getList( "" ) ) );
    }

    @Test
    public void testCleanup()
    {
        JsonObject request = new JsonObject();
        request.put( "path", PATH);
        request.put( "filesystems",
                     Arrays.asList( "maven:remote:central", "maven:hosted:pnc-builds", "maven:group:public" ) );

        Response response = given().contentType( ContentType.JSON )
                                   .body( request.toString() )
                                   .post( API_BASE + "/filesystem/cleanup" )
                                   .then()
                                   .extract()
                                   .response();

        assertEquals( 200, response.statusCode() );

    }

    @Test
    public void testFileSystemContaining()
    {
        JsonArray request = new JsonArray();
        request.add( "maven:remote:central" );
        request.add( "maven:group:public" );
        System.out.println(">>>" + request);
        Response response = given().contentType( ContentType.JSON )
                                   .body( request.toString() )
                                   .pathParam( "path", PATH)
                                   .get( API_BASE + "/filesystem/containing/{path}" )
                                   .then()
                                   .extract()
                                   .response();

        assertEquals( 200, response.statusCode() );
        assertEquals( "maven:remote:central",
                                 String.join( ",", response.jsonPath().getList( "" ) ) );
    }

}
