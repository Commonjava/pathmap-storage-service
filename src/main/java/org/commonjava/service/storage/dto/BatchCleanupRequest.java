package org.commonjava.service.storage.dto;

import java.util.Set;

public class BatchCleanupRequest
{
    private String path;

    private Set<String> filesystems;

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public Set<String> getFilesystems()
    {
        return filesystems;
    }

    public void setFilesystems(Set<String> filesystems)
    {
        this.filesystems = filesystems;
    }

    @Override
    public String toString() {
        return "BatchCleanupRequest{" +
                "path='" + path + '\'' +
                ", filesystems=" + filesystems +
                '}';
    }
}
