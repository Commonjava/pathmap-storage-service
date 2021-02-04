package org.commonjava.service.storage.jaxrs;

import java.util.Set;

public class PathMappedCleanupRequest
{

    private String path;

    private Set<String> repositories;

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public Set<String> getRepositories()
    {
        return repositories;
    }

    public void setRepositories( Set<String> repositories )
    {
        this.repositories = repositories;
    }

    @Override
    public String toString()
    {
        return "PathMappedCleanupRequest{" + "path='" + path + '\'' + ", repositories=" + repositories + '}';
    }
}
