package org.huwtl.penfold.listener.domain

import org.huwtl.penfold.listener.app.DateTimeSource
import org.joda.time.DateTime

class DateTimeSourceStub extends DateTimeSource
{
    private DateTime now;

    DateTimeSourceStub(final DateTime now)
    {
        this.now = now;
    }

    void set(final DateTime now)
    {
        this.now = now;
    }

    @Override DateTime now()
    {
        return now;
    }
}
