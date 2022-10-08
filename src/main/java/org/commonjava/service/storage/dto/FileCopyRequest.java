package org.commonjava.service.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public class FileCopyRequest
{
    private Set<String> paths;

    @JsonIgnore
    private boolean allowOverride;

    private String sourceFilesystem;

    private String targetFilesystem;

    public Set<String> getPaths() {
        return paths;
    }

    public void setPaths(Set<String> paths) {
        this.paths = paths;
    }

    public boolean isAllowOverride() {
        return allowOverride;
    }

    public void setAllowOverride(boolean allowOverride) {
        this.allowOverride = allowOverride;
    }

    public String getSourceFilesystem() {
        return sourceFilesystem;
    }

    public void setSourceFilesystem(String sourceFilesystem) {
        this.sourceFilesystem = sourceFilesystem;
    }

    public String getTargetFilesystem() {
        return targetFilesystem;
    }

    public void setTargetFilesystem(String targetFilesystem) {
        this.targetFilesystem = targetFilesystem;
    }
}
