package org.huwtl.penfold.listener.domain;

import org.huwtl.penfold.listener.domain.model.Event;
import org.huwtl.penfold.listener.domain.model.EventRecord;

public interface EventHandler<E extends Event>
{
    boolean interestedIn(EventRecord<E> event);

    void handle(EventRecord<E> event);
}
