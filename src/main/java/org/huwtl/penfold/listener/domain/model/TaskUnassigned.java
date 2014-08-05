package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

public class TaskUnassigned extends Event
{
    public final Optional<String> unassignType;

    public final Optional<Patch> payloadUpdate;

    private TaskUnassigned()
    {
        unassignType = Optional.absent();
        payloadUpdate = Optional.absent();
    }

    public TaskUnassigned(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created, final Optional<String> unassignType, final Optional<Patch> payloadUpdate)
    {
        super(eventType, aggregateId, aggregateVersion, created);
        this.unassignType = unassignType;
        this.payloadUpdate = payloadUpdate;
    }
}
