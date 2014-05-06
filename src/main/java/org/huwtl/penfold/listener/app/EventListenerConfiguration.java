package org.huwtl.penfold.listener.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.huwtl.penfold.listener.app.jackson.ObjectMapperFactory;
import org.huwtl.penfold.listener.app.mysql.MysqlDataSourceFactory;
import org.huwtl.penfold.listener.app.mysql.MysqlEventStore;
import org.huwtl.penfold.listener.app.mysql.MysqlEventStoreConfiguration;
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper;
import org.huwtl.penfold.listener.domain.EventHandler;
import org.huwtl.penfold.listener.domain.EventListener;
import org.huwtl.penfold.listener.domain.EventStore;
import org.huwtl.penfold.listener.domain.EventTracker;
import org.huwtl.penfold.listener.domain.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class EventListenerConfiguration
{
    private final String trackingId;

    private Interval pollingInterval;

    private List<EventHandler<Event>> eventHandlers = new ArrayList<EventHandler<Event>>();

    private MysqlEventStoreConfiguration eventStoreConfig;

    private EventTracker eventTracker;

    private Optional<CustomDefinedValueMapper> customDefinedValueMapper = Optional.absent();

    public EventListenerConfiguration(final String trackingId)
    {
        this.trackingId = trackingId;
    }

    public EventListenerConfiguration readEventsFrom(final MysqlEventStoreConfiguration eventStoreConfig)
    {
        this.eventStoreConfig = eventStoreConfig;
        return this;
    }

    public EventListenerConfiguration withEventHandler(final EventHandler<Event> eventHandler)
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

    public EventListenerConfiguration parseCustomJsonWith(final CustomDefinedValueMapper customDefinedValueMapper)
    {
        this.customDefinedValueMapper = Optional.of(customDefinedValueMapper);
        return this;
    }

    public EventPollingScheduler build()
    {
        validateConfig();

        final EventStore eventStore = createEventStore();

        final EventListener eventListener = new EventListener(eventStore, eventTracker, eventHandlers);

        return new EventPollingScheduler(eventListener, pollingInterval);
    }

    private EventStore createEventStore()
    {
        final ObjectMapper objectMapper = createObjectMapper();

        return new MysqlEventStore(MysqlDataSourceFactory.create(eventStoreConfig), objectMapper);
    }

    private ObjectMapper createObjectMapper()
    {
        return new ObjectMapperFactory(customDefinedValueMapper).create();
    }

    private void validateConfig()
    {
        checkArgument(isNotBlank(trackingId), "missing tracker id ");
        checkArgument(eventTracker != null, "missing event tracker");
        checkArgument(pollingInterval != null, "missing polling interval");
        checkArgument(eventHandlers.isEmpty(), "missing event handlers");
    }
}
