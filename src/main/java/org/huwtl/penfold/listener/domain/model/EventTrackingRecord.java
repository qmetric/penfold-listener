package org.huwtl.penfold.listener.domain.model;

import org.joda.time.DateTime;

public class EventTrackingRecord
{
    public final EventSequenceId id;

    public final DateTime startedDate;

    public final DateTime completedDate;

    public EventTrackingRecord(final EventSequenceId id, final DateTime startedDate, final DateTime completedDate)
    {
        this.id = id;
        this.startedDate = startedDate;
        this.completedDate = completedDate;
    }

    public boolean isNotAlreadyStarted()
    {
        return !isAlreadyStarted();
    }

    public boolean isAlreadyStarted()
    {
        return startedDate != null && completedDate == null;
    }
}
