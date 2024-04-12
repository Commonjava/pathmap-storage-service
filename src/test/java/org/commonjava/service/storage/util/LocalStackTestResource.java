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
package org.commonjava.service.storage.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.util.HashMap;
import java.util.Map;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class LocalStackTestResource implements QuarkusTestResourceLifecycleManager {

    static DockerImageName dockerImageName = DockerImageName.parse("localstack/localstack:3.2.0");
    static LocalStackContainer localStackContainer = new LocalStackContainer(dockerImageName)
            .withServices(S3);

    @Override
    public Map<String, String> start() {
        localStackContainer.start();
        prepareBucket();

        HashMap<String, String> map = new HashMap<>();
        map.put("storage.type", "s3");
        map.put("storage.physicalFileExistenceCheck", "true");
        map.put("storage.bucket.name", "test");
        map.put("quarkus.s3.endpoint-override", localStackContainer.getEndpointOverride(S3).toString());
        map.put("quarkus.s3.aws.region", localStackContainer.getRegion());
        map.put("quarkus.s3.aws.credentials.type", "static");
        map.put("quarkus.s3.aws.credentials.static-provider.access-key-id", localStackContainer.getAccessKey());
        map.put("quarkus.s3.aws.credentials.static-provider.secret-access-key", localStackContainer.getSecretKey());
        return map;
    }

    private void prepareBucket()
    {
        S3Client s3 = S3Client
                .builder()
                .endpointOverride(localStackContainer.getEndpoint())
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())
                    )
                )
                .region(Region.of(localStackContainer.getRegion()))
                .build();

        s3.createBucket(CreateBucketRequest.builder().bucket("test").build());
    }

    @Override
    public void stop() {
        localStackContainer.stop();
    }

}