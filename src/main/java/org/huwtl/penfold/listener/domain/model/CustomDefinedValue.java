package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper;

import static com.google.common.base.Preconditions.checkState;

public class CustomDefinedValue
{
    private final Optional<CustomDefinedValueMapper> customMapper;

    private final String raw;

    public CustomDefinedValue(final Optional<CustomDefinedValueMapper> customMapper, final String raw)
    {
        this.customMapper = customMapper;
        this.raw = raw;
    }

    public String getAsString()
    {
        return raw;
    }

    public <T> T getAsObject(final TypeToken<T> type)
    {
        checkState(customMapper.isPresent(), "Cannot parse as custom object without a custom mapper");

        return customMapper.get().map(raw, type);
    }

    @Override public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, "customMapper");
    }

    @Override public boolean equals(final Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj, "customMapper");
    }

    @Override public String toString()
    {
        return raw;
    }
}
