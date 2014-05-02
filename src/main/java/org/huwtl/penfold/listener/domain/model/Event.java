package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public abstract class Event
{
    public final String type;

    public final String aggregateId;

    public final String aggregateVersion;

    public final DateTime created;

    protected Event()
    {
        type = null;
        aggregateId = null;
        aggregateVersion = null;
        created = null;
    }

    public Event(final String type, final String aggregateId, final String aggregateVersion, final DateTime created)
    {
        this.type = type;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.created = created;
    }
}
