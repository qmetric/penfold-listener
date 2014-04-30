package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public class TaskTriggered extends Event
{
    public TaskTriggered(final String eventType, final String aggregateId, final String aggregateVersion, final DateTime created)
    {
        super(eventType, aggregateId, aggregateVersion, created);
    }
}
