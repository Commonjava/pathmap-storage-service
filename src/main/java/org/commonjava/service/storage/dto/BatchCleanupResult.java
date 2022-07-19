package org.commonjava.service.storage.dto;

import java.util.Set;

public class BatchCleanupResult
{
    private String path;

    private Set<String> success;

    private Set<String> failures;

    public BatchCleanupResult(String path )
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

}
