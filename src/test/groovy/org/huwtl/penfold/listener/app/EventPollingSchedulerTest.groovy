package org.huwtl.penfold.listener.app

import spock.lang.Specification

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import org.huwtl.penfold.listener.domain.EventListener

class EventPollingSchedulerTest extends Specification {
    final interval = new Interval(1, TimeUnit.MINUTES)

    final scheduledExecutionService = Mock(ScheduledExecutorService)

    final listener = Mock(EventListener)

    final scheduler = new EventPollingScheduler(listener, interval, scheduledExecutionService)

    def "should periodically poll for new events"()
    {
        when:
        scheduler.start()

        then:
        1 * scheduledExecutionService.scheduleAtFixedRate(_ as Runnable, 0, interval.time, interval.unit)
    }

    def "should catch any exception when polling for new events"()
    {
        when:
        //noinspection GroovyAccessibility
        scheduler.poll()

        then:
        1 * listener.poll() >> { throw new Exception() }
        notThrown(Exception)
    }
}
