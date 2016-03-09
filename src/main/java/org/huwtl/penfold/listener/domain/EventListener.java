package org.huwtl.penfold.listener.domain;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
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
import java.util.concurrent.Callable;

import static com.github.rholder.retry.StopStrategies.stopAfterAttempt;
import static com.github.rholder.retry.WaitStrategies.fixedWait;
import static com.google.common.collect.FluentIterable.from;
import static java.util.concurrent.TimeUnit.SECONDS;

public class EventListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    private static final int DEFAULT_EVENT_EXISTENCE_MAX_ATTEMPTS = 10;

    private final EventStore eventStore;

    private final EventTracker eventTracker;

    private final List<EventHandler> eventHandlers;

    private final Optional<DateTime> cutOffDate;

    private final StartedEventTimeoutHandler startedEventTimeoutHandler;

    private final RetryerBuilder<Optional<EventRecord>> eventExistenceRetryBuilder;

    public EventListener(final EventStore eventStore, final EventTracker eventTracker, final List<EventHandler> eventHandlers, final Optional<DateTime> cutOffDate,
                         final StartedEventTimeoutHandler startedEventTimeoutHandler)
    {
        this(eventStore, eventTracker, eventHandlers, cutOffDate, startedEventTimeoutHandler, DEFAULT_EVENT_EXISTENCE_MAX_ATTEMPTS);
    }

    EventListener(final EventStore eventStore, final EventTracker eventTracker, final List<EventHandler> eventHandlers, final Optional<DateTime> cutOffDate,
                  final StartedEventTimeoutHandler startedEventTimeoutHandler, final int eventExistenceRetrievalAttempts)
    {

        this.eventStore = eventStore;
        this.eventTracker = eventTracker;
        this.eventHandlers = eventHandlers;
        this.cutOffDate = cutOffDate;
        this.startedEventTimeoutHandler = startedEventTimeoutHandler;
        this.eventExistenceRetryBuilder = RetryerBuilder.<Optional<EventRecord>>newBuilder() //
                .retryIfResult(new Predicate<Optional<EventRecord>>()
                {
                    @Override public boolean apply(final Optional<EventRecord> input)
                    {
                        return !input.isPresent();
                    }
                }) //
                .withWaitStrategy(fixedWait(1, SECONDS)) //
                .withStopStrategy(stopAfterAttempt(eventExistenceRetrievalAttempts));
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
                LOGGER.info(String.format("event %s conflict, skipping", eventId));
                break;
            }
            catch (Exception e)
            {
                LOGGER.error(String.format("error consuming event %s", eventId), e);

                eventTracker.markAsUnstarted(eventId);

                throw new RuntimeException(e);
            }
        }
    }

    private void consumeEvent(final EventSequenceId eventId) throws ConflictException
    {
        eventTracker.markAsStarted(eventId);

        final Optional<EventRecord> eventRecord = retrieveEvent(eventId);

        if (eventRecord.isPresent() && eventAfterCutOffDate(eventRecord.get().event))
        {
            handleEvent(eventRecord);
        }

        eventTracker.markAsCompleted(eventId);
    }

    private Optional<EventRecord> retrieveEvent(final EventSequenceId eventId)
    {
        try
        {
            return eventExistenceRetryBuilder.build().call(new Callable<Optional<EventRecord>>()
            {
                @Override public Optional<EventRecord> call() throws Exception
                {
                    return eventStore.retrieveBy(eventId);

                }
            });
        }
        catch (final RetryException e)
        {
            LOGGER.info(String.format("timeout waiting for event %s to exist", eventId), e);
            return Optional.absent();
        }
        catch (final Exception e)
        {
            LOGGER.error(String.format("Failed to retrieve event %s", eventId), e);
            throw new RuntimeException(e);
        }
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
        startedEventTimeoutHandler.timeoutAnyEventThatHasBeenStartedForTooLong();

        final Optional<EventSequenceId> lastEventId = eventStore.retrieveLastEventId();

        return lastEventId.isPresent() ? new NewEventsIterator(eventTracker, lastEventId.get()) : Iterators.<EventSequenceId>emptyIterator();
    }
}
