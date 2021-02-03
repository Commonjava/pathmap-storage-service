package org.commonjava.service.storage.jaxrs;

import java.util.Set;

public class PathMappedFileSystemSetResult
{

    public String path;

    public Set<String> fileSystems;

    public PathMappedFileSystemSetResult( String path, Set<String> fileSystems )
    {
        this.path = path;
        this.fileSystems = fileSystems;
    }

    public Set<String> getFileSystems()
    {
        return fileSystems;
    }

    public void setFileSystems( Set<String> fileSystems )
    {
        this.fileSystems = fileSystems;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    @Override
    public String toString()
    {
        return "PathMappedFileSystemResult{" + "path='" + path + '\'' + ", fileSystems=" + fileSystems + '}';
    }
}
