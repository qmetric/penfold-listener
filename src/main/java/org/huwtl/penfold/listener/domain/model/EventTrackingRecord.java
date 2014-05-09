package org.huwtl.penfold.listener.domain.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class EventTrackingRecord
{
    public final EventSequenceId id;

    public final TrackingStatus status;

    public EventTrackingRecord(final EventSequenceId id, final TrackingStatus status)
    {
        this.id = id;
        this.status = status;
    }

    public boolean isUnstarted()
    {
        return status == TrackingStatus.UNSTARTED;
    }

    public boolean isCompleted()
    {
        return status == TrackingStatus.COMPLETED;
    }

    public boolean isAlreadyStarted()
    {
        return status == TrackingStatus.STARTED;
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
