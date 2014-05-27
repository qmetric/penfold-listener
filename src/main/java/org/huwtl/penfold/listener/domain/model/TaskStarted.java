package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

public class TaskStarted extends Event
{
    public final Optional<String> assignee;

    public TaskStarted()
    {
        assignee = Optional.absent();
    }

    public TaskStarted(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created, final Optional<String> assignee)
    {
        super(eventType, aggregateId, aggregateVersion, created);
        this.assignee = assignee;
    }
}
