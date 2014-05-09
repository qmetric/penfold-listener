package org.huwtl.penfold.listener.domain;

import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;
import org.huwtl.penfold.listener.domain.model.EventTrackingRecord;

class NewEventsIterator extends AbstractIterator<EventSequenceId>
{
    private final EventTracker eventTracker;

    private final EventSequenceId lastEventId;

    NewEventsIterator(final EventTracker eventTracker, final EventSequenceId lastEventId)
    {
        this.eventTracker = eventTracker;
        this.lastEventId = lastEventId;
    }

    @Override protected EventSequenceId computeNext()
    {
        final Optional<EventTrackingRecord> lastTrackedEvent = eventTracker.lastTracked();

        final Optional<EventSequenceId> nextConsumableEventToRead = nextConsumableEventToRead(lastTrackedEvent);

        if (nextConsumableEventToRead.isPresent())
        {
            return nextConsumableEventToRead.get();
        }
        else
        {
            return endOfData();
        }
    }

    private Optional<EventSequenceId> nextConsumableEventToRead(final Optional<EventTrackingRecord> lastTrackedEvent)
    {
        if (!lastTrackedEvent.isPresent())
        {
            return Optional.of(EventSequenceId.first());
        }
        else if (lastTrackedEvent.get().isUnstarted() && lastTrackedEvent.get().id.value <= lastEventId.value)
        {
            return Optional.of(lastTrackedEvent.get().id);
        }
        else if (lastTrackedEvent.get().isCompleted() && lastTrackedEvent.get().id.value < lastEventId.value)
        {
            return Optional.of(lastTrackedEvent.get().id.next());
        }
        else
        {
            return Optional.absent();
        }
    }
}
