/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.service.storage.dto;

import java.util.Set;

public class FileCopyRequest
{
    private Set<String> paths;

    private boolean failWhenExists;

    private String sourceFilesystem;

    private String targetFilesystem;

    private int timeoutSeconds;

    public Set<String> getPaths() {
        return paths;
    }

    public void setPaths(Set<String> paths) {
        this.paths = paths;
    }

    public boolean isFailWhenExists() {
        return failWhenExists;
    }

    public void setFailWhenExists(boolean failWhenExists) {
        this.failWhenExists = failWhenExists;
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

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String toString() {
        return "FileCopyRequest{" +
                "paths=" + paths +
                ", failWhenExists=" + failWhenExists +
                ", sourceFilesystem='" + sourceFilesystem + '\'' +
                ", targetFilesystem='" + targetFilesystem + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                '}';
    }
}
