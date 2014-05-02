package org.huwtl.penfold.listener.domain;

import com.google.common.base.Optional;
import org.huwtl.penfold.listener.domain.model.EventRecord;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;

public interface EventStoreReader
{
    Optional<EventSequenceId> retrieveLastEventId();

    Optional<EventRecord> retrieveBy(EventSequenceId id);
}
