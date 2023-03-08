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


import java.util.Date;

public class FileInfoObj
{
    private String filesystem;

    private String path;

    private String storagePath;

    private Long fileLength;

    private Date lastModified;

    private Date expiration;

    public FileInfoObj(String filesystem, String path )
    {
        this.filesystem = filesystem;
        this.path = path;
    }

    public String getFilesystem() {
        return filesystem;
    }

    public void setFilesystem(String filesystem) {
        this.filesystem = filesystem;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public String getStoragePath()
    {
        return storagePath;
    }

    public void setStoragePath( String storagePath )
    {
        this.storagePath = storagePath;
    }

    public Long getFileLength()
    {
        return fileLength;
    }

    public void setFileLength( Long fileLength )
    {
        this.fileLength = fileLength;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
}
