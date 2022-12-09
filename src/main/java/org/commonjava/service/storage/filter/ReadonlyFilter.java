package org.commonjava.service.storage.filter;

import io.vertx.core.http.HttpServerRequest;
import org.commonjava.service.storage.config.StorageServiceConfig;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

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