package org.huwtl.penfold.listener.domain;

import com.google.common.base.Optional;
import org.huwtl.penfold.listener.domain.model.Event;
import org.huwtl.penfold.listener.domain.model.EventRecord;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;

public interface EventStore
{
    Optional<EventSequenceId> retrieveLastEventId();

    Optional<EventRecord<Event>> retrieveBy(EventSequenceId id);
}
