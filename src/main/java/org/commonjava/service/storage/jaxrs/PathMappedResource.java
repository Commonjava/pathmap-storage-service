package org.commonjava.service.storage.jaxrs;

import org.commonjava.service.storage.controller.PathMappedController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;

@Path( "/api/pathmapped" )
public class PathMappedResource
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String CONCRETE_CONTENT_PATH = "/content/{packageType}/{type: (hosted|group|remote)}/{name}/{path: (.*)}";

    @Inject
    PathMappedController controller;

    @GET
    @Path( CONCRETE_CONTENT_PATH )
    public Response doGet(
                    @PathParam( "packageType" ) String packageType,
                    @PathParam( "type" ) String type,
                    @PathParam( "name" ) String name, @PathParam( "path" ) String path,
                    @Context UriInfo uriInfo )
    {
        logger.info( "Type: {}, name: {}, path: {}", type, name, path );

        try
        {
            InputStream in = controller.openInputStream( packageType, type, name, path );

            final Response.ResponseBuilder builder = Response.ok( in );

            return builder.build();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        return Response.ok().build();
    }

}
