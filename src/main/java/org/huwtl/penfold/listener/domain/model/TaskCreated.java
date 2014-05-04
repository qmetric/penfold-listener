package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public class TaskCreated extends Event
{
    public final QueueBinding queueBinding;

    public final DateTime triggerDate;

    public final CustomDefinedValue payload;

    public final Long score;

    public TaskCreated()
    {
        queueBinding = null;
        triggerDate = null;
        payload = null;
        score = null;
    }

    public TaskCreated(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created, final QueueBinding queueBinding, final DateTime triggerDate, final CustomDefinedValue payload, final Long score)
    {
        super(eventType, aggregateId, aggregateVersion, created);
        this.queueBinding = queueBinding;
        this.triggerDate = triggerDate;
        this.payload = payload;
        this.score = score;
    }
}
