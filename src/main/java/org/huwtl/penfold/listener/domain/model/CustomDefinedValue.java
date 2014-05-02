package org.huwtl.penfold.listener.domain.model;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
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
}
