package org.commonjava.service.storage.config;

import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.of;

@Startup
@ApplicationScoped
public class CassandraConfiguration
{
    @Inject
    @ConfigProperty( name = "cassandra.enabled", defaultValue = "false" )
    private Boolean enabled;

    @Inject
    @ConfigProperty( name = "cassandra.host", defaultValue = "localhost" )
    private String cassandraHost;

    @Inject
    @ConfigProperty( name = "cassandra.port", defaultValue = "9402" )
    private int cassandraPort;

    @Inject
    @ConfigProperty( name = "cassandra.user" )
    private Optional<String> cassandraUser;

    @Inject
    @ConfigProperty( name = "cassandra.pass" )
    private Optional<String> cassandraPass;

    @Inject
    @ConfigProperty( name = "cassandra.timeoutMillis.connect", defaultValue = "60000" )
    private int connectTimeoutMillis;

    @Inject
    @ConfigProperty( name = "cassandra.timeoutMillis.read", defaultValue = "60000" )
    private int readTimeoutMillis;

    @Inject
    @ConfigProperty( name = "cassandra.retries.read", defaultValue = "3" )
    private int readRetries;

    @Inject
    @ConfigProperty( name = "cassandra.retries.write", defaultValue = "3" )
    private int writeRetries;

    @Inject
    @ConfigProperty( name = "cassandra.keyspace" )
    private Optional<String> keyspace;

    @Inject
    @ConfigProperty( name = "cassandra.replicaFactor", defaultValue = "0" )
    private int replicationFactor;

    @Inject
    @ConfigProperty( name = "cassandra.keyspaceReplicas", defaultValue = "0" )
    private int keyspaceReplicas;

    public CassandraConfiguration()
    {
    }

    @PostConstruct
    public void testProperties()
    {
        final var logger = LoggerFactory.getLogger( this.getClass() );
        logger.info( "host: {}", getCassandraHost() );
        logger.info( "keyspace: {}", getKeyspace() );
    }

    public Boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( Boolean enabled )
    {
        this.enabled = enabled;
    }

    public void setCassandraHost( String host )
    {
        cassandraHost = host;
    }

    public void setCassandraPort( Integer port )
    {
        cassandraPort = port;
    }

    public void setCassandraUser( String cassandraUser )
    {
        this.cassandraUser = of( cassandraUser );
    }

    public void setCassandraPass( String cassandraPass )
    {
        this.cassandraPass = of( cassandraPass );
    }

    public String getCassandraHost()
    {
        return cassandraHost;
    }

    public Integer getCassandraPort()
    {
        return cassandraPort;
    }

    public String getCassandraUser()
    {
        return cassandraUser.orElse( "" );
    }

    public String getCassandraPass()
    {
        return cassandraPass.orElse( "" );
    }

    public int getConnectTimeoutMillis()
    {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis( int connectTimeoutMillis )
    {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis()
    {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis( int readTimeoutMillis )
    {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public int getReadRetries()
    {
        return readRetries;
    }

    public void setReadRetries( int readRetries )
    {
        this.readRetries = readRetries;
    }

    public int getWriteRetries()
    {
        return writeRetries;
    }

    public void setWriteRetries( int writeRetries )
    {
        this.writeRetries = writeRetries;
    }

    public String getKeyspace()
    {
        return keyspace.orElse( "" );
    }

    public void setKeyspace( String keyspace )
    {
        this.keyspace = of( keyspace );
    }

    public int getReplicationFactor()
    {
        return replicationFactor;
    }

    public void setReplicationFactor( int replicationFactor )
    {
        this.replicationFactor = replicationFactor;
    }

    public int getKeyspaceReplicas()
    {
        return keyspaceReplicas;
    }

    public void setKeyspaceReplicas( int keyspaceReplicas )
    {
        this.keyspaceReplicas = keyspaceReplicas;
    }
}
