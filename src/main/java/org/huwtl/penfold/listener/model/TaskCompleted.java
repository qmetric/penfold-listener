package org.huwtl.penfold.listener.model;

import org.joda.time.DateTime;

public class TaskCompleted extends Event
{
    public TaskCompleted(final String eventType, final String aggregateId, final String aggregateVersion, final DateTime created)
    {
        super(eventType, aggregateId, aggregateVersion, created);
    }
}
