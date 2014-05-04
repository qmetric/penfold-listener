package org.huwtl.penfold.listener.app.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JacksonCustomDefinedValueMapper implements CustomDefinedValueMapper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonCustomDefinedValueMapper.class);

    private final ObjectMapper mapper;

    public JacksonCustomDefinedValueMapper(final ObjectMapper mapper)
    {
        this.mapper = mapper;
    }

    @Override public <T> T map(final String raw, final TypeToken<T> type)
    {
        try
        {
            return mapper.readValue(raw, mapper.constructType(type.getType()));
        }
        catch (IOException e)
        {
            LOGGER.error(String.format("failed to get value as object %s %s", raw, type), e);

            throw new RuntimeException(e);
        }
    }
}
