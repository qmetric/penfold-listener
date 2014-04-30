package org.huwtl.penfold.listener.app.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper;
import org.huwtl.penfold.listener.domain.model.CustomDefinedValue;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.io.IOException;

import static com.fasterxml.jackson.core.Version.unknownVersion;
import static com.google.common.base.Preconditions.checkArgument;

public class ObjectMapperFactory
{
    private final Optional<CustomDefinedValueMapper> customValueMapper;

    public ObjectMapperFactory()
    {
        customValueMapper = Optional.absent();
    }

    public ObjectMapperFactory(final CustomDefinedValueMapper customValueMapper)
    {
        checkArgument(customValueMapper != null, "Missing custom object mapper");
        this.customValueMapper = Optional.of(customValueMapper);
    }

    public ObjectMapper create()
    {
        final SimpleModule module = new SimpleModule("jacksonConfig", unknownVersion());
        module.addDeserializer(DateTime.class, new DateTimeJsonDeserializer());
        module.addDeserializer(CustomDefinedValue.class, new CustomDefinedValueJsonDeserializer());

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.registerModule(new GuavaModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    private class DateTimeJsonDeserializer extends JsonDeserializer<DateTime>
    {
        private final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().append(null, new DateTimeParser[] {
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser()
        }).toFormatter();

        @Override public DateTime deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException
        {
            return DATE_TIME_FORMATTER.parseDateTime(jp.getValueAsString());
        }
    }

    private class CustomDefinedValueJsonDeserializer extends JsonDeserializer<CustomDefinedValue>
    {
        @Override public CustomDefinedValue deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException
        {
            return new CustomDefinedValue(customValueMapper, jp.readValueAsTree().toString());
        }
    }
}
