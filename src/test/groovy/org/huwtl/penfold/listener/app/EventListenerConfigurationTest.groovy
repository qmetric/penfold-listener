package org.huwtl.penfold.listener.app
import com.codahale.metrics.health.HealthCheckRegistry
import com.google.common.base.Optional
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper
import org.huwtl.penfold.listener.domain.EventHandler
import spock.lang.Specification

import javax.sql.DataSource

import static java.util.concurrent.TimeUnit.MINUTES

@SuppressWarnings("GroovyAccessibility")
class EventListenerConfigurationTest extends Specification {

    final config = new EventListenerConfiguration("tracker id")

    def "should accept tracker id" () {
        expect:
        config.trackerId == "tracker id"
    }

    def "should accept event store config" () {
        given:
        final eventStoreDataSource = Mock(DataSource)

        when:
        config.readEventsFromMysqlEventStore(eventStoreDataSource)

        then:
        config.eventStoreDataSource == eventStoreDataSource
    }

    def "should accept multiple event handlers" () {
        when:
        config.withEventHandlers(Mock(EventHandler), Mock(EventHandler))

        then:
        config.eventHandlers.size() == 2
    }

    def "should accept how often to poll for new events" () {
        when:
        config.pollForNewEventsEvery(2, MINUTES)

        then:
        config.pollingInterval == new Interval(2, MINUTES)
    }

    def "should accept event tracker" () {
        given:
        final eventTrackerDataSource = Mock(DataSource)

        when:
        config.withMysqlEventTracker(eventTrackerDataSource)

        then:
        config.eventTrackerDataSource == eventTrackerDataSource
    }

    def "should accept custom json parsing rules"() {
        given:
        final customParser = Mock(CustomDefinedValueMapper)

        when:
        config.parseCustomJsonWith(customParser)

        then:
        config.customDefinedValueMapper == Optional.of(customParser)
    }

    def "should accept health check registry"() {
        given:
        final registry = Mock(HealthCheckRegistry)

        when:
        config.withHealthCheckRegistry(registry)

        then:
        config.getHealthCheckRegistry() == registry
    }
}
