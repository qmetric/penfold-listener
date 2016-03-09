package org.huwtl.penfold.listener.domain

import com.google.common.base.Optional
import com.mchange.v2.c3p0.ComboPooledDataSource
import groovy.sql.Sql
import org.huwtl.penfold.listener.app.mysql.MysqlEventTracker
import org.huwtl.penfold.listener.domain.model.EventRecord
import org.huwtl.penfold.listener.domain.model.EventSequenceId
import org.huwtl.penfold.listener.domain.model.TaskArchived
import org.joda.time.DateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.sql.DataSource

import static com.google.common.base.Optional.absent

class EventListenerIntegrationTest extends Specification {

    static final CUT_OFF_DATE = new DateTime(2014, 10, 1, 12, 0, 0, 0)

    static final now = new DateTime(2015, 4, 1, 12, 0, 0, 0)

    static final startedTaskTimeoutInMinutes = 10

    @Shared EventTracker eventTracker

    @Shared Sql sql

    @Shared DateTimeSourceStub dateTimeSource = new DateTimeSourceStub(now)

    final event1 = eventRecord(CUT_OFF_DATE.minusDays(1))

    final event2 = eventRecord(CUT_OFF_DATE)

    final event3 = eventRecord(CUT_OFF_DATE.plusDays(2))

    final eventStoreReader = Stub(EventStore)

    def setupSpec()
    {
        def dataSource = initDataSource()
        eventTracker = new MysqlEventTracker(initDataSource(), "tracker1", dateTimeSource)
        sql = new Sql(dataSource)
    }

    def setup()
    {
        dateTimeSource.set(now)
    }

    def cleanup()
    {
        sql.execute("DELETE FROM event_trackers")
    }

    final suitableEventHandler = eventHandler(true)

    final unsuitableEventHandler = eventHandler(false)

    final startedEventTimeoutHandler = new StartedEventTimeoutHandler2(eventTracker, dateTimeSource, startedTaskTimeoutInMinutes)

    final eventListener = new EventListener(eventStoreReader, eventTracker, [suitableEventHandler, unsuitableEventHandler], absent(), startedEventTimeoutHandler, 2)

    def "should process new events"()
    {
        given:
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(2))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.of(event2), Optional.of(event3)]

        when:
        eventListener.poll()

        then:
        1 * suitableEventHandler.handle(event1)
        1 * suitableEventHandler.handle(event2)
        1 * suitableEventHandler.handle(event3)
        0 * unsuitableEventHandler.handle(_ as EventRecord)
    }

    def "should ignore handling any events before the cut off date"()
    {
        given:
        final eventListener = new EventListener(eventStoreReader, eventTracker, [suitableEventHandler, unsuitableEventHandler], Optional.of(CUT_OFF_DATE), startedEventTimeoutHandler)
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(2))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.of(event2), Optional.of(event3)]

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(event1)
        0 * suitableEventHandler.handle(event2)
        1 * suitableEventHandler.handle(event3)
        0 * unsuitableEventHandler.handle(_ as EventRecord)
    }

    @Unroll def "should process nothing when no new events"()
    {
        given:
        eventStoreReader.retrieveLastEventId() >> lastEventId
        if (existingTrackedEvent.isPresent())
        {
            eventTracker.markAsStarted(existingTrackedEvent.get())
            eventTracker.markAsCompleted(existingTrackedEvent.get())
        }

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(_)

        where:
        existingTrackedEvent                  | lastEventId
        absent() as Optional<EventSequenceId> | absent() as Optional<EventSequenceId>
        Optional.of(new EventSequenceId(2))   | Optional.of(new EventSequenceId(2))
    }

    def "should wait for event to exist on consuming - due to possibility under heavy server load, of events being persisted with out of order ids"()
    {
        given:
        final eventRecord = new EventRecord(new EventSequenceId(3), event1.event)
        eventTracker.markAsStarted(new EventSequenceId(2))
        eventTracker.markAsCompleted(new EventSequenceId(2))
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(3))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [absent(), Optional.of(eventRecord)]

        when:
        eventListener.poll()

        then:
        1 * suitableEventHandler.handle(eventRecord)
    }

    def "should not continue to poll further events when current event has already been started"()
    {
        given:
        eventTracker.markAsStarted(new EventSequenceId(1))
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(2))

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(_)
    }

    def "should process events when last event was completed"()
    {
        given:
        eventTracker.markAsStarted(new EventSequenceId(0))
        eventTracker.markAsCompleted(new EventSequenceId(0))
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(1))
        eventStoreReader.retrieveBy(new EventSequenceId(1)) >> Optional.of(event1)

        when:
        eventListener.poll()

        then:
        1 * suitableEventHandler.handle(event1)
    }

    def "should re-process last event when unstarted"()
    {
        given:
        eventTracker.markAsStarted(new EventSequenceId(0))
        eventTracker.markAsUnstarted(new EventSequenceId(0))
        eventStoreReader.retrieveLastEventId() >>> [Optional.of(new EventSequenceId(0))]
        eventStoreReader.retrieveBy(new EventSequenceId(0)) >> Optional.of(event1)

        when:
        eventListener.poll()

        then:
        1 * suitableEventHandler.handle(event1)
    }

    def "should allow other consumers to process event on failure"()
    {
        given:
        suitableEventHandler.handle(_ as EventRecord) >> { throw new RuntimeException() }
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(0))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >> Optional.of(event1)
        assert !eventTracker.lastTracked().isPresent()

        when:
        eventListener.poll()

        then:
        eventTracker.lastTracked().isPresent()
        eventTracker.lastTracked().get().isUnstarted()
        thrown(RuntimeException)
    }

    @Unroll def "should allow consumer to start any already started task that has been worked on for too long"()
    {
        given:
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(0))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >> Optional.of(event1)

        dateTimeSource.set(timeWhenStarted)
        eventTracker.markAsStarted(new EventSequenceId(0))
        dateTimeSource.set(currentTime)

        when:
        eventListener.poll()

        then:
        (eventHandled ? 1 : 0) * suitableEventHandler.handle(event1)

        where:
        timeWhenStarted | currentTime                                                 | eventHandled
        now             | now.plusMinutes(startedTaskTimeoutInMinutes).plusSeconds(1) | true
        now             | now.plusMinutes(startedTaskTimeoutInMinutes)                | false
        now             | now                                                         | false
    }

    private def eventHandler(final boolean applicable)
    {
        final eventHandler = Mock(EventHandler)
        eventHandler.interestedIn(_) >> applicable
        eventHandler
    }

    private static def EventRecord eventRecord(final DateTime created = now)
    {
        final event = new TaskArchived(null, null, 1, created)
        final eventRecord = new EventRecord(new EventSequenceId(1), event)
        eventRecord
    }

    private static DataSource initDataSource()
    {
        final DataSource datasource = new ComboPooledDataSource()
        datasource.setDriverClass('org.hsqldb.jdbcDriver')
        datasource.setJdbcUrl("jdbc:hsqldb:mem:event_trackers;sql.syntax_mys=true")
        datasource.setUser('sa')
        datasource.setPassword('')
        datasource
    }
}
