package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public class FutureTaskCreated extends TaskCreated
{
    private FutureTaskCreated()
    {
        super();
    }

    public FutureTaskCreated(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created, final QueueBinding queueBinding, final DateTime triggerDate, final CustomDefinedValue payload, final Long score)
    {
        super(eventType, aggregateId, aggregateVersion, created, queueBinding, triggerDate, payload, score);
    }
}
