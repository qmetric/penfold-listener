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
import org.huwtl.penfold.listener.domain.model.TaskClosed
import org.huwtl.penfold.listener.domain.model.TaskCreated
import org.huwtl.penfold.listener.domain.model.TaskPayloadUpdated
import org.huwtl.penfold.listener.domain.model.TaskRequeued
import org.huwtl.penfold.listener.domain.model.TaskRescheduled
import org.huwtl.penfold.listener.domain.model.TaskStarted
import org.huwtl.penfold.listener.domain.model.TaskTriggered
import org.huwtl.penfold.listener.domain.model.TaskUnassigned
import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Unroll

class EventDeserializerTest extends Specification {
    static final created = new DateTime(2014, 2, 3, 12, 47, 54, 0)

    static final triggerDate = new DateTime(2014, 2, 3, 14, 30, 1, 0)

    static final user = "user1"

    static final reasonType = "reason1"

    static final payload = value("{\"stuff\":\"something\",\"nested\":{\"inner\":true}}")

    static final patch = new Patch([new Add("Add", "/a/b", value("{\"a\":1}"))])

    static final score = 1391437801000L

    static final updatedScore = 100L

    final CustomDefinedValueMapper customDefinedValueMapper = new JacksonCustomDefinedValueMapper(new ObjectMapper())

    final objectMapper = new ObjectMapperFactory(Optional.of(customDefinedValueMapper)).create()

    @Unroll def "should deserialize task payload update event"()
    {
        given:
        final Event event = objectMapper.readValue(this.getClass().getResource("/fixtures/events/$jsonPath").text, Event.class)

        expect:
        event == expected

        where:
        jsonPath                        | expected
        "task_created.json"             | new FutureTaskCreated("TaskCreated", "a1", 1L, created, new QueueBinding("q1"), triggerDate, payload, score)
        "future_task_created.json"      | new FutureTaskCreated("FutureTaskCreated", "a1", 1L, created, new QueueBinding("q1"), triggerDate, payload, score)
        "task_closed.json"              | new TaskClosed("TaskClosed", "a1", 1L, created, Optional.of(user), Optional.of(reasonType), Optional.of(user), Optional.of(patch))
        "task_closed_minimal.json"      | new TaskClosed("TaskClosed", "a1", 1L, created, Optional.absent(), Optional.absent(), Optional.absent(), Optional.absent())
        "task_started.json"             | new TaskStarted("TaskStarted", "a1", 1L, created, Optional.of(user), Optional.of(patch))
        "task_started_minimal.json"     | new TaskStarted("TaskStarted", "a1", 1L, created, Optional.absent(), Optional.absent())
        "task_requeued.json"            | new TaskRequeued("TaskRequeued", "a1", 1L, created, Optional.of(reasonType), Optional.of(user), Optional.of(patch), Optional.of(updatedScore))
        "task_requeued_minimal.json"    | new TaskRequeued("TaskRequeued", "a1", 1L, created, Optional.absent(), Optional.absent(), Optional.absent(), Optional.absent())
        "task_rescheduled.json"         | new TaskRescheduled("TaskRescheduled", "a1", 1L, created, triggerDate, Optional.of(reasonType), Optional.of(user), Optional.of(patch), Optional.of(updatedScore))
        "task_rescheduled_minimal.json" | new TaskRescheduled("TaskRescheduled", "a1", 1L, created, triggerDate, Optional.absent(), Optional.absent(), Optional.absent(), Optional.absent())
        "task_unassigned.json"          | new TaskUnassigned("TaskUnassigned", "a1", 1L, created, Optional.of(reasonType), Optional.of(patch))
        "task_unassigned_minimal.json"  | new TaskUnassigned("TaskUnassigned", "a1", 1L, created, Optional.absent(), Optional.absent())
        "task_archived.json"            | new TaskArchived("TaskArchived", "a1", 1L, created)
        "task_triggered.json"           | new TaskTriggered("TaskTriggered", "a1", 1L, created)
        "task_payload_updated.json"     | new TaskPayloadUpdated("TaskPayloadUpdated", "a1", 1L, created, patch, Optional.of(reasonType), Optional.of(updatedScore))
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
