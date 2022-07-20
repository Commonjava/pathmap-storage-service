package org.commonjava.service.storage.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.output.CountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

public class DTOStreamingOutPut
                implements StreamingOutput
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    private static final double NANOS_PER_SEC = 1000000000.0;

    private final ObjectMapper mapper;

    private final Object dto;

    public DTOStreamingOutPut( final ObjectMapper mapper, final Object dto )
    {
        this.mapper = mapper;
        this.dto = dto;
    }

    @Override
    public String toString()
    {
        try
        {
            return mapper.writeValueAsString( dto );
        }
        catch ( JsonProcessingException e )
        {
            logger.error( "Could not render toString() for DTO: " + dto, e );
            return String.valueOf( dto );
        }
    }

    @Override
    public void write( final OutputStream outputStream )
                    throws IOException, WebApplicationException
    {
        CountingOutputStream cout = new CountingOutputStream( outputStream );
        try
        {
            mapper.writeValue( cout, dto );
        }
        finally
        {
            logger.trace( "Wrote: {} bytes", cout.getByteCount() );
        }
    }
}
