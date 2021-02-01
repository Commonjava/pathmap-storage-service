package org.commonjava.service.storage.config;

import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;

@Startup
@ApplicationScoped
public class IndyPathMappedConfiguration
{

    @Inject
    @ConfigProperty( name = "storage.dir", defaultValue = "/opt/indy/var/lib/indy/storage" )
    public File storageDir;

    public File getStorageRootDirectory()
    {
        return storageDir;
    }

    @PostConstruct
    public void testProperties()
    {
        final var logger = LoggerFactory.getLogger( this.getClass() );
        logger.info( "baseDir: {}", getStorageRootDirectory().getAbsolutePath());
    }
}
