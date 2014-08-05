package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

public class TaskRescheduled extends Event
{
    public final DateTime triggerDate;

    public final Optional<String> rescheduleType;

    public final Optional<String> assignee;

    public final Optional<Patch> payloadUpdate;

    public final Optional<Long> score;

    private TaskRescheduled()
    {
        triggerDate = null;
        rescheduleType = Optional.absent();
        assignee = Optional.absent();
        payloadUpdate = Optional.absent();
        score = Optional.absent();
    }

    public TaskRescheduled(final String type, final String aggregateId, final long aggregateVersion, final DateTime created, final DateTime triggerDate,
                           final Optional<String> rescheduleType, final Optional<String> assignee, final Optional<Patch> payloadUpdate, final Optional<Long> score)
    {
        super(type, aggregateId, aggregateVersion, created);
        this.triggerDate = triggerDate;
        this.rescheduleType = rescheduleType;
        this.assignee = assignee;
        this.payloadUpdate = payloadUpdate;
        this.score = score;
    }
}
