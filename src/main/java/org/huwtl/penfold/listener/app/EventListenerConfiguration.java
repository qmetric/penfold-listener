package org.huwtl.penfold.listener.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.huwtl.penfold.listener.domain.EventHandler;
import org.huwtl.penfold.listener.domain.EventListener;
import org.huwtl.penfold.listener.domain.EventStore;
import org.huwtl.penfold.listener.domain.EventTracker;
import org.huwtl.penfold.listener.domain.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

public class EventListenerConfiguration
{
    private final String trackingId;

    private Interval pollingInterval;

    private List<EventHandler<Event>> eventHandlers = new ArrayList<EventHandler<Event>>();

    private EventStore eventStore;

    private EventTracker eventTracker;

    private Optional<ObjectMapper> customObjectMapper = Optional.absent();

    public EventListenerConfiguration(final String trackingId)
    {

        this.trackingId = trackingId;
    }

    public EventListenerConfiguration from(final EventStore eventStore)
    {
        this.eventStore = eventStore;
        return this;
    }

    public EventListenerConfiguration addEventHandler(final EventHandler<Event> eventHandler)
    {
        eventHandlers.add(eventHandler);
        return this;
    }

    public EventListenerConfiguration pollForNewEventsEvery(final long interval, final TimeUnit intervalUnit)
    {
        pollingInterval = new Interval(interval, intervalUnit);
        return this;
    }

    public EventListenerConfiguration withEventTracker(final EventTracker eventTracker)
    {
        this.eventTracker = eventTracker;
        return this;
    }


    public EventListenerConfiguration withCustomObjectMapper(final ObjectMapper customObjectMapper)
    {
        this.customObjectMapper = Optional.of(customObjectMapper);
        return this;
    }

    public EventPollingScheduler build()
    {
        checkArgument(StringUtils.isNotBlank(trackingId), "tracker id missing");
        checkArgument(StringUtils.isNotBlank(trackingId), "tracker id missing");

        final EventListener eventListener = new EventListener(eventStore, eventTracker, eventHandlers);

        return new EventPollingScheduler(eventListener, pollingInterval);
    }

    public void start()
    {

    }

}
