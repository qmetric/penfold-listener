package org.huwtl.penfold.listener.domain;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.huwtl.penfold.listener.domain.model.Event;
import org.huwtl.penfold.listener.domain.model.EventRecord;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;

public class EventListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    private final EventStore eventStore;

    private final EventTracker eventTracker;

    private final List<EventHandler> eventHandlers;

    private final Optional<DateTime> cutOffDate;

    public EventListener(final EventStore eventStore, final EventTracker eventTracker, final List<EventHandler> eventHandlers, final Optional<DateTime> cutOffDate)
    {
        this.eventStore = eventStore;
        this.eventTracker = eventTracker;
        this.eventHandlers = eventHandlers;
        this.cutOffDate = cutOffDate;
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
                break;
            }
            catch (Exception e)
            {
                LOGGER.error("error consuming event", e);

                eventTracker.markAsUnstarted(eventId);

                throw new RuntimeException(e);
            }
        }
    }

    private void consumeEvent(final EventSequenceId eventId) throws ConflictException
    {
        eventTracker.markAsStarted(eventId);

        final Optional<EventRecord> eventRecord = eventStore.retrieveBy(eventId);

        if (eventRecord.isPresent() && eventAfterCutOffDate(eventRecord.get().event))
        {
            handleEvent(eventRecord);
        }

        eventTracker.markAsCompleted(eventId);
    }

    private boolean eventAfterCutOffDate(final Event event)
    {
        return !cutOffDate.isPresent() || event.created.isAfter(cutOffDate.get());
    }

    private void handleEvent(final Optional<EventRecord> eventRecord)
    {
        final Optional<EventHandler> suitableEventHandler = suitableEventHandler(eventRecord);

        if (suitableEventHandler.isPresent())
        {
            //noinspection unchecked
            suitableEventHandler.get().handle(eventRecord.get());
        }
        else
        {
            LOGGER.info("Event not handled - ignoring {}", eventRecord);
        }
    }

    private Optional<EventHandler> suitableEventHandler(final Optional<EventRecord> eventRecord)
    {
        return from(eventHandlers).firstMatch(new Predicate<EventHandler>()
                {
                    @Override
                    public boolean apply(final EventHandler eventEventHandler)
                    {
                        final EventRecord record = eventRecord.get();

                        return record.event != null && eventEventHandler.interestedIn(record.event);
                    }
                });
    }

    private Iterator<EventSequenceId> getNewEvents()
    {
        final Optional<EventSequenceId> lastEventId = eventStore.retrieveLastEventId();

        return lastEventId.isPresent() ? new NewEventsIterator(eventTracker, lastEventId.get()) : Iterators.<EventSequenceId>emptyIterator();
    }

}
