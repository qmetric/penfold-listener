package org.huwtl.penfold.listener.domain;

import org.huwtl.penfold.listener.domain.model.EventRecord;

public interface EventHandler
{
    void handle(EventRecord event);
}
