package org.commonjava.service.storage.dto;


public class FileInfoObj
{
    private String filesystem;

    private String path;

    private String storagePath;

    private Long fileLength;

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

}
