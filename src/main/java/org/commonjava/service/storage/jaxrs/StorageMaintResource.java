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
package org.commonjava.service.storage.jaxrs;

import org.commonjava.service.storage.config.StorageServiceConfig;
import org.commonjava.service.storage.controller.StorageController;
import org.commonjava.service.storage.dto.BatchDeleteResult;
import org.commonjava.service.storage.util.ResponseHelper;
import org.commonjava.storage.pathmapped.model.Filesystem;
import org.commonjava.service.storage.dto.BatchDeleteRequest;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.Collection;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Tag( name = "Storage maintenance api", description = "Resource for storage" )
@ApplicationScoped
@Path( StorageMaintResource.API_MAINT_BASE)
public class StorageMaintResource
{
    public final static String API_MAINT_BASE = "/api/storage/maint";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    StorageController controller;

    @Inject
    StorageServiceConfig config;

    @Inject
    ResponseHelper responseHelper;

    @Operation( summary = "Get empty filesystems." )
    @APIResponses( { @APIResponse( responseCode = "200", description = "The empty filesystems." ) } )
    @Produces( APPLICATION_JSON )
    @GET
    @Path( "filesystems/empty" )
    public Response getEmptyFilesystems()
    {
        logger.info( "Get empty filesystems" );
        Collection<? extends Filesystem> result = controller.getEmptyFilesystems();
        logger.debug( "Get empty filesystems, result: {}", result );
        return Response.ok( result ).build();
    }

    @Operation( summary = "Purge empty filesystems." )
    @APIResponses( { @APIResponse( responseCode = "200", description = "Purge done." ) } )
    @DELETE
    @Path( "filesystems/empty" )
    public Response purgeEmptyFilesystems()
    {
        logger.info( "Purge empty filesystems" );
        controller.purgeEmptyFilesystems();
        return Response.ok().build();
    }

    @Operation( summary = "Purge specified filesystem." )
    @APIResponses( { @APIResponse( responseCode = "200", description = "Purge done." ) } )
    @DELETE
    @Path( "filesystem/{filesystem}" )
    public Response purgeFilesystem( final @PathParam( "filesystem" ) String filesystem )
    {
        logger.info( "Purge filesystem: {}", filesystem );
        if ( !filesystem.matches( config.removableFilesystemPattern() ) )
        {
            String msg = String.format( "Purge filesystem failed (not removable), filesystem: %s", filesystem );
            logger.warn( msg );
            return responseHelper.formatBadRequestResponse( msg );
        }
        BatchDeleteResult result = controller.purgeFilesystem( filesystem );
        logger.debug( "Purge filesystem result: {}", result );
        return responseHelper.formatOkResponseWithJsonEntity( result );
    }

    /**
     * Cleans up multiple empty folders in a filesystem.
     *
     * @param request BatchDeleteRequest containing the filesystem and the set of folder paths to attempt to clean up.
     *                Only empty folders will be deleted; non-empty folders will be skipped.
     *                If a parent folder becomes empty as a result, it will also be deleted recursively up to the root.
     */
    @Operation( summary = "Cleanup multiple empty folders in a filesystem. Always returns 200 OK for easier client handling; "
                    + "failures are reported in the result object." )
    @APIResponses( { @APIResponse( responseCode = "200",
                    description = "Cleanup done (some folders may have failed, see result object)." ) } )
    @Consumes( APPLICATION_JSON )
    @Produces( APPLICATION_JSON )
    @DELETE
    @Path( "folders/empty" )
    public Response cleanupEmptyFolders( BatchDeleteRequest request )
    {
        logger.info( "Cleanup empty folders: filesystem={}, paths={}", request.getFilesystem(), request.getPaths() );
        BatchDeleteResult result = controller.cleanupEmptyFolders( request.getFilesystem(), request.getPaths() );
        return Response.ok(result).build();
    }
}
