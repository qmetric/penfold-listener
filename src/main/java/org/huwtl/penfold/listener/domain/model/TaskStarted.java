package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public class TaskStarted extends Event
{
    private TaskStarted()
    {
    }

    public TaskStarted(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created)
    {
        super(eventType, aggregateId, aggregateVersion, created);
    }
}
