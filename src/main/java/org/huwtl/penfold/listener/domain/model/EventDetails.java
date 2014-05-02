package org.huwtl.penfold.listener.domain.model;

public class EventDetails
{
    public final EventSequenceId id;

    public final Event event;

    public EventDetails(final EventSequenceId id, final Event event)
    {
        this.id = id;
        this.event = event;
    }
}
