package org.huwtl.penfold.listener.domain

import com.google.common.base.Optional
import org.huwtl.penfold.listener.domain.model.EventRecord
import org.huwtl.penfold.listener.domain.model.EventSequenceId
import org.huwtl.penfold.listener.domain.model.EventTrackingRecord
import org.joda.time.DateTime
import spock.lang.Specification

class EventListenerTest extends Specification {
    final event1 = Mock(EventRecord)

    final event2 = Mock(EventRecord)

    final event3 = Mock(EventRecord)

    final eventStoreReader = Mock(EventStore)

    final eventTracker = Mock(EventTracker)

    final suitableEventHandler = eventTracker(true)

    final unsuitableEventHandler = eventTracker(false)

    final eventListener = new EventListener(eventStoreReader, eventTracker, [suitableEventHandler, unsuitableEventHandler])

    def "should process new events"()
    {
        given:
        eventTracker.lastTracked() >>> [Optional.absent(), Optional.of(trackingRecord(0)), Optional.of(trackingRecord(1)), Optional.of(trackingRecord(2))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(2))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.of(event2), Optional.of(event3), Optional.absent()]

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

    def "should process nothing when no new events"()
    {
        given:
        eventTracker.lastTracked() >>> lastTrackedRecords
        eventStoreReader.retrieveLastEventId() >> lastEventId
        eventStoreReader.retrieveBy(_ as EventSequenceId) >> Optional.absent()

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(_)

        where:
        lastTrackedRecords                                               | lastEventId
        [Optional.absent()]                                              | Optional.absent()
        [Optional.of(trackingRecord(2))]                                 | Optional.of(new EventSequenceId(2))
        [Optional.of(trackingRecord(2)), Optional.of(trackingRecord(3))] | Optional.of(new EventSequenceId(3))
    }

    def "should not continue to poll further events when conflict exception thrown when attempting to start processing"()
    {
        given:
        final eventId = new EventSequenceId(0)
        eventTracker.markAsStarted(eventId) >> {throw new ConflictException()}
        eventTracker.lastTracked() >>> [Optional.absent(), Optional.of(trackingRecord(0))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(eventId)
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.absent()]

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(_)
    }

    def "should not continue to poll further events when current event has already been started"()
    {
        given:
        eventTracker.lastTracked() >>> [Optional.of(trackingRecord(0, true))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(10))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.absent()]

        when:
        eventListener.poll()

        then:
        0 * suitableEventHandler.handle(_)
    }

    def "should process events when last event was completed"()
    {
        given:
        eventTracker.lastTracked() >>> [Optional.of(trackingRecord(0, true, true)), Optional.of(trackingRecord(1))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(1))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.absent()]

        when:
        eventListener.poll()

        then:
        1 * suitableEventHandler.handle(event1)
    }

    def "should allow other consumers to process event on failure"()
    {
        given:
        suitableEventHandler.handle(_ as EventRecord) >> {throw new RuntimeException()}
        eventTracker.lastTracked() >>> [Optional.absent(), Optional.of(trackingRecord(0))]
        eventStoreReader.retrieveLastEventId() >> Optional.of(new EventSequenceId(0))
        eventStoreReader.retrieveBy(_ as EventSequenceId) >>> [Optional.of(event1), Optional.absent()]

        when:
        eventListener.poll()

        then:
        1 * eventTracker.markAsStarted(new EventSequenceId(0))
        1 * eventTracker.markAsUnstarted(new EventSequenceId(0))
        0 * eventTracker.markAsCompleted(new EventSequenceId(0))
    }

    private def eventTracker(final boolean applicable)
    {
        final eventHandler = Mock(EventHandler)
        eventHandler.interestedIn(_ as EventRecord) >> applicable
        eventHandler
    }

    private static def EventTrackingRecord trackingRecord(final long eventId, final boolean started = false, final boolean completed = false)
    {
        new EventTrackingRecord(new EventSequenceId(eventId), started ? Optional.of(DateTime.now()): Optional.<DateTime>absent(), completed ? Optional.of(DateTime.now()): Optional.<DateTime>absent())
    }
}
