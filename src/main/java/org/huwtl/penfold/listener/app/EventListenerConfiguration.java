package org.huwtl.penfold.listener.app;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.huwtl.penfold.listener.app.health.ConnectivityHealthCheck;
import org.huwtl.penfold.listener.app.jackson.ObjectMapperFactory;
import org.huwtl.penfold.listener.app.mysql.MysqlEventStore;
import org.huwtl.penfold.listener.app.mysql.MysqlEventTracker;
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper;
import org.huwtl.penfold.listener.domain.EventHandler;
import org.huwtl.penfold.listener.domain.EventListener;
import org.huwtl.penfold.listener.domain.EventStore;
import org.huwtl.penfold.listener.domain.EventTracker;
import org.huwtl.penfold.listener.domain.StartedEventTimeoutHandler2;
import org.joda.time.DateTime;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.copyOf;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class EventListenerConfiguration
{
    private final String trackerId;

    private Interval pollingInterval;

    private List<EventHandler> eventHandlers = new ArrayList<EventHandler>();

    private DataSource eventStoreDataSource;

    private DataSource eventTrackerDataSource;

    private Optional<DateTime> cutOffDate = Optional.absent();

    private Optional<CustomDefinedValueMapper> customDefinedValueMapper = Optional.absent();

    private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    public EventListenerConfiguration(final String trackerId)
    {
        this.trackerId = trackerId;
    }

    public EventListenerConfiguration readEventsFromMysqlEventStore(final DataSource dataSource)
    {
        this.eventStoreDataSource = dataSource;
        return this;
    }

    public EventListenerConfiguration withEventHandlers(final EventHandler... eventHandlers)
    {
        this.eventHandlers.addAll(copyOf(eventHandlers));
        return this;
    }

    public EventListenerConfiguration pollForNewEventsEvery(final long interval, final TimeUnit intervalUnit)
    {
        pollingInterval = new Interval(interval, intervalUnit);
        return this;
    }

    public EventListenerConfiguration withMysqlEventTracker(final DataSource dataSource)
    {
        this.eventTrackerDataSource = dataSource;
        return this;
    }

    public EventListenerConfiguration ignoreEventsEarlierThan(final DateTime cutOffDate)
    {
        this.cutOffDate = Optional.of(cutOffDate);
        return this;
    }

    public EventListenerConfiguration parseCustomJsonWith(final CustomDefinedValueMapper customDefinedValueMapper)
    {
        this.customDefinedValueMapper = Optional.of(customDefinedValueMapper);
        return this;
    }

    public EventListenerConfiguration withHealthCheckRegistry(final HealthCheckRegistry healthCheckRegistry)
    {
        this.healthCheckRegistry = healthCheckRegistry;

        return this;
    }

    public HealthCheckRegistry getHealthCheckRegistry()
    {
        return healthCheckRegistry;
    }

    public EventPollingScheduler build()
    {
        validateConfig();

        final EventStore eventStore = createEventStore();

        final EventTracker eventTracker = new MysqlEventTracker(eventTrackerDataSource, trackerId);

        final EventListener eventListener = new EventListener(eventStore, eventTracker, eventHandlers, cutOffDate, new StartedEventTimeoutHandler2(eventTracker));

        registerHealthChecks(eventStore, eventTracker);

        return new EventPollingScheduler(eventListener, pollingInterval);
    }

    private void registerHealthChecks(final EventStore eventStore, final EventTracker eventTracker)
    {
        healthCheckRegistry.register("Event store", new ConnectivityHealthCheck(eventStore));
        healthCheckRegistry.register("Event tracker", new ConnectivityHealthCheck(eventTracker));
    }

    private EventStore createEventStore()
    {
        final ObjectMapper objectMapper = createObjectMapper();

        return new MysqlEventStore(eventStoreDataSource, objectMapper);
    }

    private ObjectMapper createObjectMapper()
    {
        return new ObjectMapperFactory(customDefinedValueMapper).create();
    }

    private void validateConfig()
    {
        checkArgument(isNotBlank(trackerId), "missing tracker id ");
        checkArgument(eventStoreDataSource != null, "missing event store data source");
        checkArgument(eventTrackerDataSource != null, "missing event tracker data source");
        checkArgument(pollingInterval != null, "missing polling interval");
        checkArgument(!eventHandlers.isEmpty(), "missing event handlers");
    }
}
