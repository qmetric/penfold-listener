package org.huwtl.penfold.listener.app;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class Interval
{
    public final long time;

    public final TimeUnit unit;

    public Interval(final long time, final TimeUnit unit)
    {
        checkArgument(time > 0 && unit != null, "Invalid interval");

        this.time = time;
        this.unit = unit;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return reflectionEquals(this, obj);
    }

    @Override public String toString()
    {
        return reflectionToString(this);
    }
}