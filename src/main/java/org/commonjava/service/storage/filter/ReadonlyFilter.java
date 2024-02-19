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
package org.commonjava.service.storage.filter;

import io.vertx.core.http.HttpServerRequest;
import org.commonjava.service.storage.config.StorageServiceConfig;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ReadonlyFilter implements ContainerRequestFilter
{
    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter( ContainerRequestContext context )
    {
        // filter is not a bean, so we can not Inject config but get it via CDI api.
        StorageServiceConfig serviceConfig = CDI.current().select(StorageServiceConfig.class).get();
        if ( serviceConfig.readonly() )
        {
            // only allow GET methods
            if ( !context.getMethod().equals(HttpMethod.GET) )
            {
                context.abortWith( Response.status(Response.Status.FORBIDDEN)
                        .header("Storage-Service-Mode", "readonly").build() );
            }
        }
    }
}