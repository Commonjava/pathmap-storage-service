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

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.commonjava.service.storage.util.LocalStackTestResource;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.commonjava.service.storage.jaxrs.StorageResource.API_BASE;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(LocalStackTestResource.class)
public class S3StorageIT
                extends StorageIT
{
    @Inject
    S3Client s3Client;

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

        verifyBucket();
    }

    private void verifyBucket()
    {
        ListObjectsRequest lor = ListObjectsRequest.builder().bucket("test").build();
        ListObjectsResponse response = s3Client.listObjects(lor);
        List<S3Object> contents = response.contents();

        assertTrue(contents.size() > 0 );
        for (S3Object et: contents)
        {
            //System.out.println( ">>> " + et.key());
            assertTrue(et.key().contains(PATH));
        }
    }
}
