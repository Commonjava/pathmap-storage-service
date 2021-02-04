package org.commonjava.service.storage.jaxrs;

import java.util.Set;

public class PathMappedCleanupResult
{

    private String path;

    private Set<String> success;

    private Set<String> failures;

    public PathMappedCleanupResult( String path )
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public Set<String> getSuccess()
    {
        return success;
    }

    public void setSuccess( Set<String> success )
    {
        this.success = success;
    }

    public Set<String> getFailures()
    {
        return failures;
    }

    public void setFailures( Set<String> failures )
    {
        this.failures = failures;
    }

    @Override
    public String toString()
    {
        return "PathMappedCleanupResult{" + "path='" + path + '\'' + ", success=" + success + ", failures=" + failures
                        + '}';
    }
}
