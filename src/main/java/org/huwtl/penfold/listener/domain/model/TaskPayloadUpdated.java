package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

public class TaskPayloadUpdated extends Event
{
    public final Patch payloadUpdate;

    public final Optional<String> updateType;

    public final Optional<Long> score;

    private TaskPayloadUpdated()
    {
       payloadUpdate = null;
        updateType = null;
        score = null;
    }

    public TaskPayloadUpdated(final String eventType, final String aggregateId, final String aggregateVersion, final DateTime created, final Patch payloadUpdate, final Optional<String> updateType, final Optional<Long> score)
    {
        super(eventType, aggregateId, aggregateVersion, created);
        this.payloadUpdate = payloadUpdate;
        this.updateType = updateType;
        this.score = score;
    }
}
