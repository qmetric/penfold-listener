package org.huwtl.penfold.listener.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.huwtl.penfold.listener.app.jackson.JacksonCustomDefinedValueMapper
import org.huwtl.penfold.listener.app.jackson.ObjectMapperFactory
import spock.lang.Specification

class EventListenerTest extends Specification {

    final EventListener eventListener = new EventListener(new ObjectMapperFactory(new JacksonCustomDefinedValueMapper(new ObjectMapper())).create())

    def "should parse event"()
    {
        when:
        eventListener.listenForNewEvents(this.getClass().getResource("/fixtures/events/task_payload_updated.json").text)

        then:
        true
    }
}
