package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public class TaskCancelled extends Event
{
    private TaskCancelled()
    {
        super();
    }

    public TaskCancelled(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created)
    {
        super(eventType, aggregateId, aggregateVersion, created);
    }
}
