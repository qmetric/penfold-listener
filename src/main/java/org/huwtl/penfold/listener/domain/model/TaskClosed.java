package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

public class TaskClosed extends Event
{
    public final Optional<String> concluder;

    public final Optional<String> conclusionType;

    public final Optional<String> assignee;

    public final Optional<Patch> payloadUpdate;

    public TaskClosed()
    {
        concluder = Optional.absent();
        conclusionType = Optional.absent();
        assignee = Optional.absent();
        payloadUpdate = Optional.absent();
    }

    public TaskClosed(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created, final Optional<String> concluder,
                      final Optional<String> conclusionType, final Optional<String> assignee, final Optional<Patch> payloadUpdate)
    {
        super(eventType, aggregateId, aggregateVersion, created);
        this.concluder = concluder;
        this.conclusionType = conclusionType;
        this.assignee = assignee;
        this.payloadUpdate = payloadUpdate;
    }
}
