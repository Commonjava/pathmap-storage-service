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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.commonjava.service.storage.dto.BatchDeleteRequest;
import org.commonjava.service.storage.dto.BatchDeleteResult;
import org.commonjava.service.storage.dto.BatchExistResult;
import org.commonjava.service.storage.dto.FileCopyResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;
import static org.commonjava.service.storage.jaxrs.StorageMaintResource.API_MAINT_BASE;
import static org.commonjava.service.storage.jaxrs.StorageResource.API_BASE;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StorageResourceIT
                extends StorageIT
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
    public void testPutFile()
    {
        given().pathParam( "filesystem", filesystem )
                .pathParam( "path", PATH)
                .when()
                .put( API_BASE + "/content/{filesystem}/{path}" )
                .then()
                .statusCode( 200 );
    }

    @Test
    public void testPutFileWithTimeout() throws InterruptedException
    {
        given().pathParam( "filesystem", filesystem )
                .pathParam( "path", PATH)
                .queryParam( "timeout", "1s" )
                .when()
                .put( API_BASE + "/content/{filesystem}/{path}" )
                .then()
                .statusCode( 200 );

        sleep(3000);

        given().pathParam( "filesystem", filesystem )
                .pathParam( "path", PATH)
                .when()
                .get( API_BASE + "/content/{filesystem}/{path}" )
                .then()
                .statusCode( 404 );
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
    public void testGetPathsByChecksum() throws Exception
    {
        // filesystem must match the 'deduplicatePattern'
        final String g_filesystem = "generic-http:remote:g_filesystem";
        final String checksum = "209094962790ca300e4e387f4b3dd130ff06ef9a9c3e08c2f96889334db4cf4c";

        // upload the file to g_filesystem
        try (InputStream in = url.openStream())
        {
            prepareFile( in, g_filesystem, PATH );
        }

        // test with right checksum
        Response response = given().pathParam( "checksum", checksum )
                                   .when()
                                   .get( API_BASE + "/checksum/{checksum}" )
                                   .then()
                                   .extract()
                                   .response();

        assertEquals( 200, response.statusCode() );
        //System.out.println(">>>" + response.jsonPath().getList( "" ));
        assertTrue( response.jsonPath().getList( "" ).contains( g_filesystem + ":/" + PATH ) );

        // test with wrong checksum
        response = given().pathParam( "checksum", "wrong-094962790ca30" )
                          .when()
                          .get( API_BASE + "/checksum/{checksum}" )
                          .then()
                          .extract()
                          .response();

        assertEquals( 200, response.statusCode() );
        //System.out.println(">>>" + response.getBody().asString() );
        assertTrue( response.jsonPath().getList( "" ).isEmpty() );
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
        Response response = doList();

        List<String> expectedList = Arrays.asList( "io/", "io/quarkus/", "io/quarkus/quarkus-junit5/",
                                                   "io/quarkus/quarkus-junit5/quarkus-junit5-1.12.0.Final.jar" );

        assertEquals( 200, response.statusCode() );
        assertEquals( String.join( ",", expectedList ),
                                 String.join( ",", response.jsonPath().getList( "" ) ) );
    }

    private Response doList()
    {
        return given().pathParam( "filesystem", filesystem )
                .queryParam( "recursive", true )
                .when()
                .get( API_BASE + "/browse/{filesystem}" )
                .then()
                .extract()
                .response();
    }

    @Test
    public void testListAndBatchDelete()
            throws Exception
    {
        Response response = doList();
        assertEquals( 200, response.statusCode() );

        List<String> list = response.jsonPath().getList("");

        BatchDeleteRequest request = new BatchDeleteRequest();
        request.setFilesystem(filesystem);
        request.setPaths(new HashSet<>(list));

        // Batch deletion
        response = doDelete(new ObjectMapper().writeValueAsString( request ));
        assertEquals( 200, response.statusCode() );

        List<String> expectedList = Arrays.asList( "io/", "io/quarkus/", "io/quarkus/quarkus-junit5/",
                "io/quarkus/quarkus-junit5/quarkus-junit5-1.12.0.Final.jar" );

        BatchDeleteResult result = response.getBody().as( BatchDeleteResult.class );
        //System.out.println(">>>" + result);
        assertTrue( result.getFailed().isEmpty() );
        assertTrue( result.getSucceeded().containsAll( expectedList ));
        assertTrue( result.getFilesystem().equals( filesystem ));
    }

    @Test
    public void testPurge()
            throws Exception
    {
        Response response = given()
                .pathParam( "filesystem", filesystem)
                .delete( API_MAINT_BASE + "/filesystem/{filesystem}" )
                .then()
                .extract()
                .response();

        assertEquals( 200, response.statusCode() );

        List<String> expectedList = Arrays.asList( "io/", "io/quarkus/", "io/quarkus/quarkus-junit5/",
                "io/quarkus/quarkus-junit5/quarkus-junit5-1.12.0.Final.jar" );

        BatchDeleteResult result = response.getBody().as( BatchDeleteResult.class );
        //System.out.println(">>>" + result);
        assertTrue( result.getFailed().isEmpty() );
        assertTrue( result.getSucceeded().containsAll( expectedList ));
        assertTrue( result.getFilesystem().equals( filesystem ));
    }

    private Response doDelete(String json)
    {
        return given().contentType( ContentType.JSON )
                .body( json )
                .post( API_BASE + "/filesystem" )
                .then()
                .extract()
                .response();
    }

    @Test
    public void testCleanup()
    {
        JsonObject request = new JsonObject();
        request.put( "paths", Arrays.asList(PATH));
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
    public void testBatchDelete()
    {
        JsonObject request = new JsonObject();
        request.put( "filesystem", filesystem );
        request.put( "paths", Arrays.asList( PATH ) );

        Response response = doDelete( request.toString() );
        assertEquals( 200, response.statusCode() );

        BatchDeleteResult result = response.getBody().as( BatchDeleteResult.class );
        assertTrue( result.getFailed().isEmpty() );
        assertTrue( result.getSucceeded().contains( PATH ));
        assertTrue( result.getFilesystem().equals( filesystem ));
    }

    @Test
    public void testBatchExist()
    {
        String nonExistPath = "some/path/not/exist";
        JsonObject request = new JsonObject();
        request.put( "filesystem", filesystem );
        request.put( "paths", Arrays.asList( PATH, nonExistPath ) );

        Response response = given().contentType( ContentType.JSON )
                .body( request.toString() )
                .post( API_BASE + "/filesystem/exist" )
                .then()
                .extract()
                .response();

        assertEquals( 200, response.statusCode() );

        BatchExistResult result = response.getBody().as( BatchExistResult.class );
        assertTrue( result.getMissing().size() == 1 );
        assertTrue( result.getMissing().contains(nonExistPath));
        assertTrue( result.getFilesystem().equals( filesystem ));
    }

    @Test
    public void testFileSystemContaining()
    {
        JsonArray request = new JsonArray();
        request.add( "maven:remote:central" );
        request.add( "maven:group:public" );
        //System.out.println(">>>" + request);
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

    @Test
    public void testCopy()
    {
        JsonObject request = new JsonObject();
        request.put( "sourceFilesystem", "maven:remote:central" );
        request.put( "targetFilesystem", "maven:hosted:pnc-builds" );
        request.put( "paths",
                Arrays.asList( PATH ) );

        Response response = given().contentType( ContentType.JSON )
                .body( request.toString() )
                .post( API_BASE + "/copy" )
                .then()
                .extract()
                .response();

        assertEquals( 200, response.statusCode() );
        assertTrue( response.jsonPath().getBoolean("success") );
    }

    @Test
    public void testCopyFileSkipped() throws Exception
    {
        // Put a file in target before copy
        prepareFile( new ByteArrayInputStream("this is a test".getBytes()), "maven:hosted:pnc-builds", PATH );

        JsonObject request = new JsonObject();
        request.put( "sourceFilesystem", "maven:remote:central" );
        request.put( "targetFilesystem", "maven:hosted:pnc-builds" );
        request.put( "paths",
                Arrays.asList( PATH ) );

        Response response = given().contentType( ContentType.JSON )
                .body( request.toString() )
                .post( API_BASE + "/copy" )
                .then()
                .extract()
                .response();

        assertEquals( 200, response.statusCode() );
        FileCopyResult ret = response.getBody().as(FileCopyResult.class);
        assertTrue( ret.isSuccess() );
        assertTrue( ret.getSkipped().size() == 1 );
        assertTrue( ret.getCompleted() == null || ret.getCompleted().isEmpty() );
    }

    @Test
    public void testCopyFileMissing()
    {
        JsonObject request = new JsonObject();
        request.put( "sourceFilesystem", "maven:remote:central" );
        request.put( "targetFilesystem", "maven:hosted:pnc-builds" );
        request.put( "paths",
                Arrays.asList( PATH, "a/missed/file" ) );

        Response response = given().contentType( ContentType.JSON )
                .body( request.toString() )
                .post( API_BASE + "/copy" )
                .then()
                .extract()
                .response();

        assertEquals( 200, response.statusCode() );
        assertFalse( response.jsonPath().getBoolean("success") );
        String err = response.jsonPath().getString( "message" );
        //System.out.println(">>> " + err );
        assertTrue( err.contains( "missing" ) );
    }

    /**
     * Copied file expires in 1s. Wait and get it should return 404.
     */
    @Test
    public void testCopyFileExpire() throws Exception
    {
        final String targetFilesystem = "maven:hosted:shared-imports";
        final int timeoutSeconds = 1;

        JsonObject request = new JsonObject();
        request.put( "sourceFilesystem", "maven:remote:central" );
        request.put( "targetFilesystem", targetFilesystem );
        request.put( "paths",
                Arrays.asList( PATH ) );
        request.put( "timeoutSeconds", timeoutSeconds );

        Response response = given().contentType( ContentType.JSON )
                .body( request.toString() )
                .post( API_BASE + "/copy" )
                .then()
                .extract()
                .response();

        assertEquals( 200, response.statusCode() );
        assertTrue( response.jsonPath().getBoolean("success") );

        // Sleep and wait for file expiring
        sleep(TimeUnit.SECONDS.toMillis(timeoutSeconds));

        // Get return 404
        given().pathParam( "filesystem", targetFilesystem )
                .pathParam( "path", PATH)
                .when()
                .get( API_BASE + "/content/{filesystem}/{path}" )
                .then()
                .statusCode( 404 );
    }

}
