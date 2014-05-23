package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public class TaskArchived extends Event
{
    private TaskArchived()
    {
    }

    public TaskArchived(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created)
    {
        super(eventType, aggregateId, aggregateVersion, created);
    }
}
