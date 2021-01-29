package org.commonjava.service.storage.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path( "/storage" )
public class StorageResources
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Path( "/all" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getAll()
    {

        logger.info( "API storage all." );

        return Response.ok().build();
    }

}
