package org.commonjava.service.storage.event;

public class FileEvent
{

    private String trackingID;

    private EventMetadata eventMetadata;

    public String getTrackingID()
    {
        return trackingID;
    }

    public void setTrackingID( String trackingID )
    {
        this.trackingID = trackingID;
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
