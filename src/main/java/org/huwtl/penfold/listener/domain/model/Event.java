package org.huwtl.penfold.listener.domain.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

public abstract class Event
{
    public final String type;

    public final String aggregateId;

    public final Long aggregateVersion;

    public final DateTime created;

    protected Event()
    {
        type = null;
        aggregateId = null;
        aggregateVersion = null;
        created = null;
    }

    public Event(final String type, final String aggregateId, final long aggregateVersion, final DateTime created)
    {
        this.type = type;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.created = created;
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
