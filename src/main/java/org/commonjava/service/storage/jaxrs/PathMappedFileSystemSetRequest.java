package org.commonjava.service.storage.jaxrs;

import java.util.List;

public class PathMappedFileSystemSetRequest
{

    public List<String> candidates;

    public List<String> getCandidates()
    {
        return candidates;
    }

    public void setCandidates( List<String> candidates )
    {
        this.candidates = candidates;
    }

    @Override
    public String toString()
    {
        return "PathMappedFileSystemRequest{" + "candidates=" + candidates + '}';
    }
}
