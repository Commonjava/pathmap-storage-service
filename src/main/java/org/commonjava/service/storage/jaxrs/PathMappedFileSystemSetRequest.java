package org.commonjava.service.storage.jaxrs;

import java.util.Set;

public class PathMappedFileSystemSetRequest
{

    public Set<String> candidates;

    public Set<String> getCandidates()
    {
        return candidates;
    }

    public void setCandidates( Set<String> candidates )
    {
        this.candidates = candidates;
    }

    @Override
    public String toString()
    {
        return "PathMappedFileSystemRequest{" + "candidates=" + candidates + '}';
    }
}
