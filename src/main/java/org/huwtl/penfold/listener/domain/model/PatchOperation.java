package org.huwtl.penfold.listener.domain.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class PatchOperation
{
    public final String op;

    public final String path;

    protected PatchOperation() {
        op = null;
        path = null;
    }

    protected PatchOperation(final String op, final String path)
    {
        this.op = op;
        this.path = path;
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
