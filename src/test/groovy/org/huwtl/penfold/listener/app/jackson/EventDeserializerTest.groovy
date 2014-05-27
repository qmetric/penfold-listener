package org.huwtl.penfold.listener.app.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import com.google.common.reflect.TypeToken
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper
import org.huwtl.penfold.listener.domain.model.Add
import org.huwtl.penfold.listener.domain.model.CustomDefinedValue
import org.huwtl.penfold.listener.domain.model.Event
import org.huwtl.penfold.listener.domain.model.FutureTaskCreated
import org.huwtl.penfold.listener.domain.model.Patch
import org.huwtl.penfold.listener.domain.model.QueueBinding
import org.huwtl.penfold.listener.domain.model.TaskArchived
import org.huwtl.penfold.listener.domain.model.TaskCancelled
import org.huwtl.penfold.listener.domain.model.TaskCompleted
import org.huwtl.penfold.listener.domain.model.TaskCreated
import org.huwtl.penfold.listener.domain.model.TaskPayloadUpdated
import org.huwtl.penfold.listener.domain.model.TaskRequeued
import org.huwtl.penfold.listener.domain.model.TaskStarted
import org.huwtl.penfold.listener.domain.model.TaskTriggered
import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Unroll

class EventDeserializerTest extends Specification {
    static final created = new DateTime(2014, 2, 3, 12, 47, 54, 0)

    static final triggerDate = new DateTime(2014, 2, 3, 14, 30, 1, 0)

    final CustomDefinedValueMapper customDefinedValueMapper = new JacksonCustomDefinedValueMapper(new ObjectMapper())

    final objectMapper = new ObjectMapperFactory(Optional.of(customDefinedValueMapper)).create()

    @Unroll def "should deserialize task payload update event"()
    {
        given:
        final Event event = objectMapper.readValue(this.getClass().getResource("/fixtures/events/$jsonPath").text, Event.class)

        expect:
        event == expected

        where:
        jsonPath                      | expected
        "task_created.json"           | new FutureTaskCreated("TaskCreated", "a1", 1L, created, new QueueBinding("q1"), triggerDate, value("{\"stuff\":\"something\",\"nested\":{\"inner\":true}}"), 1391437801000L)
        "future_task_created.json"    | new FutureTaskCreated("FutureTaskCreated", "a1", 1L, created, new QueueBinding("q1"), triggerDate, value("{\"stuff\":\"something\",\"nested\":{\"inner\":true}}"), 1391437801000L)
        "task_cancelled.json"         | new TaskCancelled("TaskCancelled", "a1", 1L, created)
        "task_completed.json"         | new TaskCompleted("TaskCompleted", "a1", 1L, created, Optional.of("user1"), Optional.of("reason1"))
        "task_completed_minimal.json" | new TaskCompleted("TaskCompleted", "a1", 1L, created, Optional.absent(), Optional.absent())
        "task_started.json"           | new TaskStarted("TaskStarted", "a1", 1L, created, Optional.of("user1"))
        "task_started_minimal.json"   | new TaskStarted("TaskStarted", "a1", 1L, created, Optional.absent())
        "task_requeued.json"          | new TaskRequeued("TaskRequeued", "a1", 1L, created)
        "task_archived.json"          | new TaskArchived("TaskArchived", "a1", 1L, created)
        "task_triggered.json"         | new TaskTriggered("TaskTriggered", "a1", 1L, created)
        "task_payload_updated.json"   | new TaskPayloadUpdated("TaskPayloadUpdated", "a1", 1L, created, new Patch([new Add("Add", "/a/b", value("{\"a\":1}"))]), Optional.of("update_type_1"), Optional.of(100L))
    }

    def "should deserialize payload for event using custom specific object mapper"()
    {
        given:
        final TaskCreated event = (TaskCreated) objectMapper.readValue(this.getClass().getResource("/fixtures/events/task_created.json").text, Event.class)

        expect:
        event.payload.getAsObject(TypeToken.of(Map)) == [nested: [inner: true], stuff: "something"]
    }

    @Unroll def "should deserialize patch operator value for payload update event using custom specific object mapper"()
    {
        given:
        final TaskPayloadUpdated event = (TaskPayloadUpdated) objectMapper.readValue(this.getClass().getResource("/fixtures/events/task_payload_updated.json").text, Event.class)

        expect:
        (event.payloadUpdate.operations[0] as Add).value.getAsObject(TypeToken.of(Map)) == [a: 1]
    }

    private static def value(final String value)
    {
        new CustomDefinedValue(Optional.absent(), value)
    }
}
