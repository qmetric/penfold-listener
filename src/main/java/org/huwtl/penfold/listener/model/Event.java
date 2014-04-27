package org.huwtl.penfold.listener.model;

import org.joda.time.DateTime;

public abstract class Event
{
    public final String eventType;

    public final String aggregateId;

    public final String aggregateVersion;

    public final DateTime created;

    public Event(final String eventType, final String aggregateId, final String aggregateVersion, final DateTime created)
    {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.created = created;
    }
}
