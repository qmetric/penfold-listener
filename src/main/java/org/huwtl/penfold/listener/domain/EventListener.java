package org.huwtl.penfold.listener.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.huwtl.penfold.listener.domain.model.Event;

import java.io.IOException;
import java.util.Iterator;

public class EventListener
{
    final ObjectMapper mapper;

    public EventListener(final ObjectMapper mapper)
    {
        this.mapper = mapper;
    }

    public Iterator<Event> listenForNewEvents(final String eventJson)
    {
        try
        {
            final ImmutableList<Event> events = ImmutableList.of(mapper.readValue(eventJson, Event.class));

            return events.iterator();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
