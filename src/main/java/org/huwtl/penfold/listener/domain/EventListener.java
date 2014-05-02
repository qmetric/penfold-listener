package org.huwtl.penfold.listener.domain;

import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import org.huwtl.penfold.listener.domain.model.EventRecord;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;
import org.huwtl.penfold.listener.domain.model.EventTrackingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class EventListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    private final EventStoreReader eventStore;

    private final EventTracker eventTracker;

    private final EventHandler eventHandler;

    public EventListener(final EventStoreReader eventStore, final EventTracker eventTracker, final EventHandler eventHandler)
    {
        this.eventStore = eventStore;
        this.eventTracker = eventTracker;
        this.eventHandler = eventHandler;
    }

    public void poll()
    {
        final Iterator<EventSequenceId> newEventIds = getNewEvents();

        while (newEventIds.hasNext())
        {
            final EventSequenceId eventId = newEventIds.next();

            try
            {
                LOGGER.info(String.format("attempting to consume event %s", eventId));

                consumeEvent(eventId);

                LOGGER.info(String.format("successfully consumed event %s", eventId));
            }
            catch (ConflictException e)
            {
                LOGGER.info(String.format("event conflict, skipping"));
            }
            catch (Exception e)
            {
                LOGGER.error("error consuming event", e);

                eventTracker.markAsUnstarted(eventId);
            }
        }
    }

    private void consumeEvent(final EventSequenceId eventId) throws ConflictException
    {
        eventTracker.markAsStarted(eventId);

        final Optional<EventRecord> eventRecord = eventStore.retrieveBy(eventId);

        if (eventRecord.isPresent())
        {
            eventHandler.handle(eventRecord.get());
        }

        eventTracker.markAsCompleted(eventId);
    }

    private Iterator<EventSequenceId> getNewEvents()
    {
        final Optional<EventSequenceId> lastEventId = eventStore.retrieveLastEventId();

        return lastEventId.isPresent() ? new NewEventsIterator(eventTracker, lastEventId.get()) : Iterators.<EventSequenceId>emptyIterator();
    }

    private static class NewEventsIterator extends AbstractIterator<EventSequenceId>
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
            else if (lastTrackedEvent.get().isNotAlreadyStarted() && lastTrackedEvent.get().id.value < lastEventId.value)
            {
                return Optional.of(lastTrackedEvent.get().id.next());
            }
            else
            {
                return Optional.absent();
            }
        }
    }
}
