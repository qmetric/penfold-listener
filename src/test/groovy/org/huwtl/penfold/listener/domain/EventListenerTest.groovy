package org.huwtl.penfold.listener.domain

import com.google.common.base.Optional
import org.huwtl.penfold.listener.domain.model.*
import org.joda.time.DateTime
import spock.lang.Specification

import static com.google.common.base.Optional.absent
import static org.huwtl.penfold.listener.domain.model.TrackingStatus.*

class EventListenerTest extends Specification {

    static final CUT_OFF_DATE = new DateTime(2014, 10, 1, 12, 0, 0, 0)

    final event1 = eventRecord(CUT_OFF_DATE.minusDays(1))

    final event2 = eventRecord(CUT_OFF_DATE)

    final event3 = eventRecord(CUT_OFF_DATE.plusDays(2))

    final eventStoreReader = Mock(EventStore)

    final eventTracker = Mock(EventTracker)

    final suitableEventHandler = eventTracker(true)

    final unsuitableEventHandler = eventTracker(false)

    final eventListener = new EventListener(eventStoreReader, eventTracker, [suitableEventHandler, unsuitableEventHandler], absent())

    def "should process new events"()
    {
        given:
        eventTracker.lastTracked() >>> [absent(), Optional.of(trackingRecord(0, COMPLETED)), Optional.of(trackingRecord(1, COMPLETED)), Optional.of(trackingRecord(2, COMPLETED))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(2))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.of(event2), Optional.of(event3), absent()]

        when:
        eventListener.poll()

        then:
        1 * eventTracker.markAsStarted(new EventSequenceId(0))
        1 * suitableEventHandler.handle(event1)
        1 * eventTracker.markAsCompleted(new EventSequenceId(0))

        1 * eventTracker.markAsStarted(new EventSequenceId(1))
        1 * suitableEventHandler.handle(event2)
        1 * eventTracker.markAsCompleted(new EventSequenceId(1))

        1 * eventTracker.markAsStarted(new EventSequenceId(2))
        1 * suitableEventHandler.handle(event3)
        1 * eventTracker.markAsCompleted(new EventSequenceId(2))

        0 * unsuitableEventHandler.handle(_ as EventRecord)
    }

    def "should ignore handling any events before the cut off date"()
    {
        given:
        final eventListener = new EventListener(eventStoreReader, eventTracker, [suitableEventHandler, unsuitableEventHandler], Optional.of(CUT_OFF_DATE))
        eventTracker.lastTracked() >>> [absent(), Optional.of(trackingRecord(0, COMPLETED)), Optional.of(trackingRecord(1, COMPLETED)), Optional.of(trackingRecord(2, COMPLETED))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(2))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.of(event2), Optional.of(event3), absent()]

        when:
        eventListener.poll()

        then:
        1 * eventTracker.markAsStarted(new EventSequenceId(0))
        0 * suitableEventHandler.handle(event1)
        1 * eventTracker.markAsCompleted(new EventSequenceId(0))

        1 * eventTracker.markAsStarted(new EventSequenceId(1))
        0 * suitableEventHandler.handle(event2)
        1 * eventTracker.markAsCompleted(new EventSequenceId(1))

        1 * eventTracker.markAsStarted(new EventSequenceId(2))
        1 * suitableEventHandler.handle(event3)
        1 * eventTracker.markAsCompleted(new EventSequenceId(2))

        0 * unsuitableEventHandler.handle(_ as EventRecord)
    }

    def "should process nothing when no new events"()
    {
        given:
        eventTracker.lastTracked() >>> lastTrackedRecords
        eventStoreReader.retrieveLastEventId() >> lastEventId
        eventStoreReader.retrieveBy(_ as EventSequenceId) >> absent()

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(_)

        where:
        lastTrackedRecords                                                                     | lastEventId
        [absent()]                                                                             | absent()
        [Optional.of(trackingRecord(2, COMPLETED))]                                            | Optional.of(new EventSequenceId(2))
        [Optional.of(trackingRecord(2, COMPLETED)), Optional.of(trackingRecord(3, COMPLETED))] | Optional.of(new EventSequenceId(3))
    }

    def "should not continue to poll further events when conflict exception thrown when attempting to start processing"()
    {
        given:
        final eventId = new EventSequenceId(0)
        eventTracker.markAsStarted(eventId) >> { throw new ConflictException() }
        eventTracker.lastTracked() >>> [absent(), Optional.of(trackingRecord(0, COMPLETED))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(eventId)
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), absent()]

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(_)
    }

    def "should not continue to poll further events when current event has already been started"()
    {
        given:
        eventTracker.lastTracked() >>> [Optional.of(trackingRecord(0, STARTED))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(10))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), absent()]

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(_)
    }

    def "should process events when last event was completed"()
    {
        given:
        eventTracker.lastTracked() >>> [Optional.of(trackingRecord(0, COMPLETED)), Optional.of(trackingRecord(1, COMPLETED))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(1))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), absent()]

        when:
        eventListener.poll()

        then:
        1 * suitableEventHandler.handle(event1)
    }

    def "should reprocess last event was unstarted"()
    {
        given:
        eventTracker.lastTracked() >>> [Optional.of(trackingRecord(0, UNSTARTED)), Optional.of(trackingRecord(0, COMPLETED))]
        eventStoreReader.retrieveLastEventId() >>> [Optional.of(new EventSequenceId(0))]
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), absent()]

        when:
        eventListener.poll()

        then:
        1 * suitableEventHandler.handle(event1)
    }

    def "should allow other consumers to process event on failure"()
    {
        given:
        suitableEventHandler.handle(_ as EventRecord) >> { throw new RuntimeException() }
        eventTracker.lastTracked() >>> [absent(), Optional.of(trackingRecord(0, COMPLETED))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(0))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), absent()]

        when:
        eventListener.poll()

        then:
        1 * eventTracker.markAsStarted(new EventSequenceId(0))
        1 * eventTracker.markAsUnstarted(new EventSequenceId(0))
        0 * eventTracker.markAsCompleted(new EventSequenceId(0))
        thrown(RuntimeException)
    }

    private def eventTracker(final boolean applicable)
    {
        final eventHandler = Mock(EventHandler)
        eventHandler.interestedIn(_) >> applicable
        eventHandler
    }

    private static def EventTrackingRecord trackingRecord(final long eventId, final TrackingStatus status)
    {
        new EventTrackingRecord(new EventSequenceId(eventId), status)
    }

    private static def EventRecord eventRecord(final DateTime created = DateTime.now())
    {
        final event = new TaskArchived(null, null, 1, created)
        final eventRecord = new EventRecord(new EventSequenceId(1), event)
        eventRecord
    }
}
