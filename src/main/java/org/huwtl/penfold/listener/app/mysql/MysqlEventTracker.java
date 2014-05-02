package org.huwtl.penfold.listener.app.mysql;

import com.google.common.base.Optional;
import org.huwtl.penfold.listener.domain.ConflictException;
import org.huwtl.penfold.listener.domain.EventTracker;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;
import org.huwtl.penfold.listener.domain.model.EventTrackingRecord;

public class MysqlEventTracker implements EventTracker
{
    @Override public Optional<EventTrackingRecord> lastTracked()
    {
        return null;
    }

    @Override public void markAsStarted(final EventSequenceId id) throws ConflictException
    {

    }

    @Override public void markAsUnstarted(final EventSequenceId id)
    {

    }

    @Override public void markAsCompleted(final EventSequenceId id)
    {

    }
}
