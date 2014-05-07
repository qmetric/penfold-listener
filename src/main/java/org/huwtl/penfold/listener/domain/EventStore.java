package org.huwtl.penfold.listener.domain;

import com.google.common.base.Optional;
import org.huwtl.penfold.listener.app.mysql.ConnectivityAware;
import org.huwtl.penfold.listener.domain.model.EventRecord;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;

public interface EventStore extends ConnectivityAware
{
    Optional<EventSequenceId> retrieveLastEventId();

    Optional<EventRecord> retrieveBy(EventSequenceId id);
}
