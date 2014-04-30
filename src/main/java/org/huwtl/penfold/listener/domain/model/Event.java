package org.huwtl.penfold.listener.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.joda.time.DateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true, property = "type")
@JsonSubTypes({
                  @JsonSubTypes.Type(value = TaskCreated.class, name = "TaskCreated"),
                  @JsonSubTypes.Type(value = FutureTaskCreated.class, name = "FutureTaskCreated"),
                  @JsonSubTypes.Type(value = TaskTriggered.class, name = "TaskTriggered"),
                  @JsonSubTypes.Type(value = TaskStarted.class, name = "TaskStarted"),
                  @JsonSubTypes.Type(value = TaskCompleted.class, name = "TaskCompleted"),
                  @JsonSubTypes.Type(value = TaskCancelled.class, name = "TaskCancelled"),
                  @JsonSubTypes.Type(value = TaskPayloadUpdated.class, name = "TaskPayloadUpdated")
              })
public abstract class Event
{
    public final String type;

    public final String aggregateId;

    public final String aggregateVersion;

    public final DateTime created;

    protected Event()
    {
        type = null;
        aggregateId = null;
        aggregateVersion = null;
        created = null;
    }

    public Event(final String type, final String aggregateId, final String aggregateVersion, final DateTime created)
    {
        this.type = type;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.created = created;
    }
}
