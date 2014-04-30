package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper;

import static com.google.common.base.Preconditions.checkState;

public class CustomDefinedValue
{
    private final Optional<CustomDefinedValueMapper> mapper;

    private final String raw;

    public CustomDefinedValue(final Optional<CustomDefinedValueMapper> mapper, final String raw)
    {
        this.mapper = mapper;
        this.raw = raw;
    }

    public String getAsString()
    {
        return raw;
    }

    public <T> T getAsObject(final TypeToken<T> type)
    {
        checkState(mapper.isPresent(), "Cannot parse as custom object without a custom mapper");

        return mapper.get().map(raw, type);
    }
}
