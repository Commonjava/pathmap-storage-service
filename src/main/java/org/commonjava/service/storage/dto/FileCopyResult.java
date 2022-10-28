package org.commonjava.service.storage.dto;

import java.util.Set;

public class FileCopyResult
{
    private boolean success;

    private Set<String> completed;

    private Set<String> skipped;

    private String message;

    public FileCopyResult() {
    }

    public FileCopyResult(boolean success ) {
        this.success = success;
    }

    public FileCopyResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public FileCopyResult(boolean success, Set<String> completed, Set<String> skipped) {
        this.success = success;
        this.completed = completed;
        this.skipped = skipped;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<String> getCompleted() {
        return completed;
    }

    public void setCompleted(Set<String> completed) {
        this.completed = completed;
    }

    public Set<String> getSkipped() {
        return skipped;
    }

    public void setSkipped(Set<String> skipped) {
        this.skipped = skipped;
    }
}
