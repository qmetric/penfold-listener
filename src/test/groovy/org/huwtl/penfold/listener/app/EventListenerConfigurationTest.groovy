package org.huwtl.penfold.listener.app

import com.google.common.base.Optional
import org.huwtl.penfold.listener.app.mysql.MysqlEventStoreConfiguration
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper
import org.huwtl.penfold.listener.domain.EventHandler
import org.huwtl.penfold.listener.domain.EventTracker
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.MINUTES

@SuppressWarnings("GroovyAccessibility")
class EventListenerConfigurationTest extends Specification {

    final config = new EventListenerConfiguration("tracking id")

    def "should accept tracking id" () {
        expect:
        config.trackingId == "tracking id"
    }

    def "should accept event store config" () {
        given:
        final eventStoreConfig = Mock(MysqlEventStoreConfiguration)

        when:
        config.readEventsFrom(eventStoreConfig)

        then:
        config.eventStoreConfig == eventStoreConfig
    }

    def "should accept multiple event handlers" () {
        when:
        config.withEventHandler(Mock(EventHandler))
        config.withEventHandler(Mock(EventHandler))

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
        final eventTracker = Mock(EventTracker)

        when:
        config.withEventTracker(eventTracker)

        then:
        config.eventTracker == eventTracker
    }

    def "should accept custom json parsing rules"() {
        given:
        final customParser = Mock(CustomDefinedValueMapper)

        when:
        config.parseCustomJsonWith(customParser)

        then:
        config.customDefinedValueMapper == Optional.of(customParser)
    }
}
