package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

public class EventTrackingRecord
{
    public final EventSequenceId id;

    public final Optional<DateTime> startedDate;

    public final Optional<DateTime> completedDate;

    public EventTrackingRecord(final EventSequenceId id, final Optional<DateTime> startedDate, final Optional<DateTime> completedDate)
    {
        this.id = id;
        this.startedDate = startedDate;
        this.completedDate = completedDate;
    }

    public boolean isNotAlreadyStarted()
    {
        return !isAlreadyStarted();
    }

    public boolean isAlreadyStarted()
    {
        return startedDate.isPresent() && !completedDate.isPresent();
    }

    @Override public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override public boolean equals(final Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
