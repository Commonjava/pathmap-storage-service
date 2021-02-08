package org.commonjava.service.storage.event;

public class FileEvent
{

    private String tackingID;

    private EventMetadata eventMetadata;

    public String getTackingID()
    {
        return tackingID;
    }

    public void setTackingID( String tackingID )
    {
        this.tackingID = tackingID;
    }

    public EventMetadata getEventMetadata()
    {
        return eventMetadata;
    }

    public void setEventMetadata( EventMetadata eventMetadata )
    {
        this.eventMetadata = eventMetadata;
    }
}
