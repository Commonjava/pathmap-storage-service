package org.commonjava.service.storage.config;

import io.quarkus.runtime.Startup;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;

@Startup
@ApplicationScoped
public class IndyPathMappedConfiguration
{

    public static final File DEFAULT_BASEDIR = new File( "/opt/indy/var/lib/indy/storage" );

    public File getStorageRootDirectory()
    {
        return DEFAULT_BASEDIR;
    }

    @PostConstruct
    public void testProperties()
    {
        final var logger = LoggerFactory.getLogger( this.getClass() );
        logger.info( "baseDir: {}", getStorageRootDirectory().getAbsolutePath());
    }
}
