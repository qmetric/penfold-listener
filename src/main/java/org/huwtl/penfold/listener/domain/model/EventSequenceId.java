package org.huwtl.penfold.listener.domain.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class EventSequenceId
{
    public final long value;

    public EventSequenceId(final long value)
    {
        this.value = value;
    }

    public EventSequenceId next()
    {
        return new EventSequenceId(value + 1);
    }

    public static EventSequenceId first()
    {
        return new EventSequenceId(0);
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
