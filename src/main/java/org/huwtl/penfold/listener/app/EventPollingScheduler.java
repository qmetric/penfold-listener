package org.huwtl.penfold.listener.app;

import org.huwtl.penfold.listener.domain.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class EventPollingScheduler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EventPollingScheduler.class);

    private final EventListener eventListener;

    private final Interval interval;

    private final ScheduledExecutorService scheduledExecutorService;

    private final ShutdownProcedure shutdownProcedure;

    public EventPollingScheduler(final EventListener eventListener, final Interval pollingInterval)
    {
        this(eventListener, pollingInterval, newSingleThreadScheduledExecutor());
    }

    EventPollingScheduler(final EventListener eventListener, final Interval pollingInterval, final ScheduledExecutorService executorService)
    {
        this.eventListener = eventListener;
        this.interval = pollingInterval;
        this.scheduledExecutorService = executorService;
        this.shutdownProcedure = new ShutdownProcedure(executorService);
    }

    public void start()
    {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                poll();
            }
        }, 0, interval.time, interval.unit);

        shutdownProcedure.registerShutdownHook();
    }

    public void stop()
    {
        shutdownProcedure.runAndRemoveHook();
    }

    private void poll()
    {
        try
        {
            eventListener.poll();
        }
        catch (final Exception e)
        {
            LOGGER.error("error occurred whilst polling for new events", e);
        }
    }
}
