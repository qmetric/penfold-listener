package org.huwtl.penfold.listener.model;

import org.joda.time.DateTime;

public class TaskStarted extends Event
{
    public TaskStarted(final String eventType, final String aggregateId, final String aggregateVersion, final DateTime created)
    {
        super(eventType, aggregateId, aggregateVersion, created);
    }
}
