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
