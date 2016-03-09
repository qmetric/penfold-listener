package org.huwtl.penfold.listener.domain;

import com.google.common.base.Optional;
import org.huwtl.penfold.listener.app.DateTimeSource;
import org.huwtl.penfold.listener.domain.model.EventTrackingRecord;

public class StartedEventTimeoutHandler
{
    private static final int DEFAULT_TIMEOUT_IN_MINUTES = 10;

    private final EventTracker eventTracker;

    private final DateTimeSource dateTimeSource;

    private final int timeoutInMinutes;

    public StartedEventTimeoutHandler(final EventTracker eventTracker)
    {
        this(eventTracker, new DateTimeSource(), DEFAULT_TIMEOUT_IN_MINUTES);
    }

    public StartedEventTimeoutHandler(final EventTracker eventTracker, final DateTimeSource dateTimeSource, final int timeoutInMinutes)
    {
        this.eventTracker = eventTracker;
        this.dateTimeSource = dateTimeSource;
        this.timeoutInMinutes = timeoutInMinutes;
    }

    public void timeoutAnyEventThatHasBeenStartedForTooLong()
    {
        final Optional<EventTrackingRecord> eventTracking = eventTracker.lastTracked();
        if (eventTracking.isPresent() && eventTracking.get().isAlreadyStartedAndTimedOut(dateTimeSource.now(), timeoutInMinutes))
        {
            eventTracker.markAsUnstarted(eventTracking.get().id);
        }
    }
}
