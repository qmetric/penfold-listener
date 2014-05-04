package org.huwtl.penfold.listener.domain;

import com.google.common.base.Optional;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;
import org.huwtl.penfold.listener.domain.model.EventTrackingRecord;

public interface EventTracker
{
    Optional<EventTrackingRecord> lastTracked();

    void markAsStarted(final EventSequenceId id) throws ConflictException;

    void markAsUnstarted(final EventSequenceId id);

    void markAsCompleted(final EventSequenceId id);
}
