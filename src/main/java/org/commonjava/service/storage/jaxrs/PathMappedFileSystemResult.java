package org.commonjava.service.storage.jaxrs;


public class PathMappedFileSystemResult
{

    private String packageType;

    private String type;

    private String name;

    private String path;

    private String storagePath;

    private Long fileLength;

    public PathMappedFileSystemResult( String packageType, String type, String name, String path )
    {
        this.packageType = packageType;
        this.type = type;
        this.name = name;
        this.path = path;
    }

    public String getPackageType()
    {
        return packageType;
    }

    public void setPackageType( String packageType )
    {
        this.packageType = packageType;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
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

    @Override
    public String toString()
    {
        return "PathMappedFileSystemResult{" + "packageType='" + packageType + '\'' + ", type='" + type + '\''
                        + ", name='" + name + '\'' + ", path='" + path + '\'' + ", storagePath='" + storagePath + '\''
                        + ", fileLength=" + fileLength + '}';
    }
}
