package org.commonjava.service.storage.jaxrs;

import org.commonjava.service.storage.controller.PathMappedController;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.PATH;
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
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
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
    @Operation( summary = "Get the file by the specified path." )
    @APIResponses( { @APIResponse ( responseCode = "200", description = "The request file.") } )
    @Produces( APPLICATION_OCTET_STREAM )
    public Response doGet(
                    final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
                    final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group", "remote" } ),
                                    required = true ) @PathParam( "type" ) String type,
                    final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name, @PathParam( "path" ) String path,
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
            final String message = "Failed to get the file " + path + ".";

            logger.error( message, e );
            return responseHelper.formatResponse( e, message );
        }

    }

    @PUT
    @Path( CONCRETE_CONTENT_PATH )
    @Operation( summary = "Store the file." )
    @APIResponses( { @APIResponse ( responseCode = "200", description = "The file is stored.") })
    public Response doCreate(
                    final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
                    final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                                    "remote" } ), required = true ) @PathParam( "type" ) String type,
                    final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
                    final @Parameter( in = PATH, required = true ) @PathParam( "path" ) String path,
                    final @Context UriInfo uriInfo, final @Context HttpRequest request )
    {
        try
        {
            controller.create( packageType, type, name, path, request );
        }
        catch ( IOException e )
        {
            final String message = "Failed to store the file " + path + ".";

            logger.error( message, e );
            return responseHelper.formatResponse( e, message );
        }

        return Response.ok().build();
    }

    @DELETE
    @Path( CONCRETE_CONTENT_PATH )
    @Operation( summary = "Delete the file." )
    @APIResponses( { @APIResponse ( responseCode = "200", description = "The file is removed.") })
    public Response doDelete(
                    final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
                    final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                                    "remote" } ), required = true ) @PathParam( "type" ) String type,
                    final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
                    final @Parameter( in = PATH, required = true ) @PathParam( "path" ) String path,
                    @Context UriInfo uriInfo )
    {
        logger.info( "Delete. type: {}, name: {}, path: {}", type, name, path );

        controller.delete( packageType, type, name, path );

        return Response.ok().build();
    }

    @GET
    @Path( BROWSE_BASE )
    @Operation( summary = "List the files under the repository root directory." )
    @APIResponses( { @APIResponse( responseCode = "200",
                    content = @Content( schema = @Schema( implementation = PathMappedListResult.class ) ),
                    description = "The files under the repository root directory." ) } )
    @Produces( APPLICATION_JSON )
    public Response listRoot(
                    final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
                    final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                                    "remote" } ), required = true ) @PathParam( "type" ) String type,
                    final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
                    final @Parameter( in = PATH, required = true ) @PathParam( "path" ) String path,
                    final @QueryParam( "recursive" ) boolean recursive, final @QueryParam( "type" ) String fileType,
                    final @QueryParam( "limit" ) int limit )
    {
        logger.info( "List, packageType:{}, type:{}, name:{}, path:{}, recursive:{}", packageType, type, name, path,
                      recursive );
        PathMappedListResult result = controller.list( packageType, type, name, ROOT_DIR, recursive, fileType, limit );
        return responseHelper.formatOkResponseWithJsonEntity( result );
    }

    @GET
    @Path( BROWSE_BASE + "/{path: (.*)}" )
    @Operation( summary = "List the files under the specified path." )
    @APIResponses( { @APIResponse( responseCode = "200",
                    content = @Content( schema = @Schema( implementation = PathMappedListResult.class ) ),
                    description = "The files under the path." ) } )
    @Produces( APPLICATION_JSON )
    public Response list( final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
                          final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                                          "remote" } ), required = true ) @PathParam( "type" ) String type,
                          final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
                          final @Parameter( in = PATH, required = true ) @PathParam( "path" ) String path,
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
    @Operation( summary = "Get the repositories containing the specified path." )
    @RequestBody( description = "The filesystems collection in JSON", name = "body", required = true,
                    content = @Content( schema = @Schema( implementation = PathMappedFileSystemSetRequest.class ) ) )
    @APIResponses( { @APIResponse( responseCode = "200",
                    content = @Content( schema = @Schema( implementation = PathMappedFileSystemSetResult.class ) ),
                    description = "The filesystems that containing the file." ) } )
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
    @Operation( summary = "Get the detailed info of the file." )
    @APIResponses( { @APIResponse( responseCode = "200",
                    content = @Content( schema = @Schema( implementation = PathMappedFileSystemResult.class ) ),
                    description = "The detailed info of the file." ) } )
    @Produces( APPLICATION_JSON )
    public Response doGetFileInfo(
                    final @Parameter( in = PATH, required = true ) @PathParam( "packageType" ) String packageType,
                    final @Parameter( in = PATH, schema = @Schema( enumeration = { "hosted", "group",
                                    "remote" } ), required = true ) @PathParam( "type" ) String type,
                    final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
                    final @Parameter( in = PATH, required = true ) @PathParam( "path" ) String path,
                    @Context UriInfo uriInfo )
    {
        logger.info( "Type: {}, name: {}, path: {}", type, name, path );

        PathMappedFileSystemResult result = controller.getFileInfo( packageType, type, name, path );

        logger.info( "File info: {}", result );

        return responseHelper.formatOkResponseWithJsonEntity( result );
    }

    @POST
    @Path( "/filesystem/cleanup" )
    @Operation( summary = "Cleanup the files under the specified repositories." )
    @RequestBody( description = "The cleanup request definition JSON", name = "body", required = true,
                    content = @Content( schema = @Schema( implementation = PathMappedCleanupRequest.class ) ) )
    @APIResponses( { @APIResponse( responseCode = "200",
                    content = @Content( schema = @Schema( implementation = PathMappedCleanupResult.class ) ),
                    description = "The status of cleaning files." ) } )
    @Consumes( APPLICATION_JSON )
    @Produces( APPLICATION_JSON )
    public Response doCleanup( final PathMappedCleanupRequest request )
    {
        logger.info( "Cleanup, path:{}, fileSystems:{}", request.getPath(), request.getRepositories() );

        PathMappedCleanupResult result = controller.cleanup( request.getPath(), request.getRepositories() );

        logger.info( "Cleanup result: {}", result );

        return responseHelper.formatOkResponseWithJsonEntity( result );
    }

}
