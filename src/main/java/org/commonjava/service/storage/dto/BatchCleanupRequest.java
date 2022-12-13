package org.commonjava.service.storage.dto;

import java.util.Set;

/**
 * Cleanup multiple paths from multiple filesystems.
 */
public class BatchCleanupRequest
{
    private Set<String> paths;

    private Set<String> filesystems;

    public Set<String> getFilesystems()
    {
        return filesystems;
    }

    public void setFilesystems(Set<String> filesystems)
    {
        this.filesystems = filesystems;
    }

    public Set<String> getPaths() {
        return paths;
    }

    public void setPaths(Set<String> paths) {
        this.paths = paths;
    }

    @Override
    public String toString() {
        return "BatchCleanupRequest{" +
                "paths=" + paths +
                ", filesystems=" + filesystems +
                '}';
    }
}
