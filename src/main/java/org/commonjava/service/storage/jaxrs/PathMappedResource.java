package org.commonjava.service.storage.jaxrs;

import org.commonjava.service.storage.controller.PathMappedController;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.commonjava.storage.pathmapped.util.PathMapUtils.ROOT_DIR;

@Path( "/api/pathmapped" )
public class PathMappedResource
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String BROWSE_BASE = "/browse/{packageType}/{type: (hosted|group|remote)}/{name}";

    private static final String CONCRETE_CONTENT_PATH = "/content/{packageType}/{type: (hosted|group|remote)}/{name}/{path: (.*)}";

    @Inject
    ResponseHelper responseHelper;

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

    @PUT
    @Path( CONCRETE_CONTENT_PATH )
    public Response doCreate(
                    final @PathParam( "packageType" ) String packageType,
                    final @PathParam( "type" )
                                    String type, final @PathParam( "name" ) String name,
                    final @PathParam( "path" ) String path, final @Context UriInfo uriInfo,
                    final @Context HttpRequest request )
    {
        controller.create( packageType, type, name, path, request );

        return Response.ok().build();
    }

    @DELETE
    @Path( CONCRETE_CONTENT_PATH )
    public Response doDelete(
                    @PathParam( "packageType" ) String packageType,
                    @PathParam( "type" ) String type,
                    @PathParam( "name" ) String name, @PathParam( "path" ) String path,
                    @Context UriInfo uriInfo )
    {
        logger.info( "Delete. type: {}, name: {}, path: {}", type, name, path );

        controller.delete( packageType, type, name, path );

        return Response.ok().build();
    }


    @GET
    @Path( BROWSE_BASE )
    @Produces( APPLICATION_JSON )
    public Response listRoot( final @PathParam( "packageType" ) String packageType,
                          final @PathParam( "type" ) String type,
                          final @PathParam( "name" ) String name,
                          final @PathParam( "path" ) String path,
                          final @QueryParam( "recursive" ) boolean recursive,
                          final @QueryParam( "type" ) String fileType,
                          final @QueryParam( "limit" ) int limit )
    {
        logger.info( "List, packageType:{}, type:{}, name:{}, path:{}, recursive:{}", packageType, type, name, path,
                      recursive );
        PathMappedListResult result = controller.list( packageType, type, name, ROOT_DIR, recursive, fileType, limit );
        return responseHelper.formatOkResponseWithJsonEntity( result );
    }

    @GET
    @Path( BROWSE_BASE + "/{path: (.*)}" )
    @Produces( APPLICATION_JSON )
    public Response list( final @PathParam( "packageType" ) String packageType,
                          final @PathParam( "type" ) String type,
                          final @PathParam( "name" ) String name,
                          final @PathParam( "path" ) String path,
                          final @QueryParam( "recursive" ) boolean recursive,
                          final @QueryParam( "type" ) String fileType,
                          final @QueryParam( "limit" ) int limit )
    {
        logger.info( "List, packageType:{}, type:{}, name:{}, path:{}, recursive:{}", packageType, type, name, path,
                      recursive );
        PathMappedListResult result = controller.list( packageType, type, name, path, recursive, fileType, limit );
        return responseHelper.formatOkResponseWithJsonEntity( result );
    }

    @POST
    @Path( "/filesystem/containing/{path: (.*)}" )
    @Consumes( APPLICATION_JSON )
    @Produces( APPLICATION_JSON )
    public Response getFileSystemContaining( final @PathParam( "path" ) String path, final PathMappedFileSystemSetRequest request )
    {

        logger.info( "FileSystemContaining, path: {}, request: {}", path, request );

        PathMappedFileSystemSetResult result = controller.getFileSystemContaining( request.getCandidates(), path);

        return responseHelper.formatOkResponseWithJsonEntity( result );
    }

    @GET
    @Path( "/filesystem" + CONCRETE_CONTENT_PATH + "/info" )
    @Produces( APPLICATION_JSON )
    public Response doGetStoragePath(
                    @PathParam( "packageType" ) String packageType,
                    @PathParam( "type" ) String type,
                    @PathParam( "name" ) String name, @PathParam( "path" ) String path,
                    @Context UriInfo uriInfo )
    {
        logger.info( "Type: {}, name: {}, path: {}", type, name, path );

        PathMappedFileSystemResult result = controller.getFileInfo( packageType, type, name, path );

        logger.info( "File info: {}", result );

        return responseHelper.formatOkResponseWithJsonEntity( result );
    }

}
