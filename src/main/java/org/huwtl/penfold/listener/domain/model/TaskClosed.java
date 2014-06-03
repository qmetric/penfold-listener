package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

public class TaskClosed extends Event
{
    public final Optional<String> concluder;

    public final Optional<String> conclusionType;

    public TaskClosed()
    {
        concluder = Optional.absent();
        conclusionType = Optional.absent();
    }

    public TaskClosed(final String eventType, final String aggregateId, final Long aggregateVersion, final DateTime created, final Optional<String> concluder,
                      final Optional<String> conclusionType)
    {
        super(eventType, aggregateId, aggregateVersion, created);
        this.concluder = concluder;
        this.conclusionType = conclusionType;
    }
}
