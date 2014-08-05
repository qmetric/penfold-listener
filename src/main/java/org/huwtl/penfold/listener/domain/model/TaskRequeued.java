package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

public class TaskRequeued extends Event
{
    public final Optional<String> requeueType;

    public final Optional<String> assignee;

    public final Optional<Patch> payloadUpdate;

    public final Optional<Long> score;

    private TaskRequeued()
    {
        requeueType = Optional.absent();
        assignee = Optional.absent();
        payloadUpdate = Optional.absent();
        score = Optional.absent();
    }

    public TaskRequeued(final String type, final String aggregateId, final long aggregateVersion, final DateTime created, final Optional<String> requeueType,
                        final Optional<String> assignee, final Optional<Patch> payloadUpdate, final Optional<Long> score)
    {
        super(type, aggregateId, aggregateVersion, created);
        this.requeueType = requeueType;
        this.assignee = assignee;
        this.payloadUpdate = payloadUpdate;
        this.score = score;
    }
}
