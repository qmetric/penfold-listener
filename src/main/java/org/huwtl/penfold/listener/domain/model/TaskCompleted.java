package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public class TaskCompleted extends Event
{
    private TaskCompleted()
    {
    }

    public TaskCompleted(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created)
    {
        super(eventType, aggregateId, aggregateVersion, created);
    }
}
