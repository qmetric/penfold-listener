package org.huwtl.penfold.listener.domain.model;

public class EventRecord
{
    public final EventSequenceId id;

    public final Event event;

    public EventRecord(final EventSequenceId id, final Event event)
    {
        this.id = id;
        this.event = event;
    }
}
