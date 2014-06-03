package org.huwtl.penfold.listener.app.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.huwtl.penfold.listener.domain.CustomDefinedValueMapper;
import org.huwtl.penfold.listener.domain.model.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.io.IOException;
import java.util.Map;

import static com.fasterxml.jackson.core.Version.unknownVersion;
import static com.google.common.base.Preconditions.checkArgument;

public class ObjectMapperFactory
{
    private final Map<String, Class<? extends Event>> supportedEvents = new ImmutableMap.Builder<String, Class<? extends Event>>() //
            .put("TaskPayloadUpdated", TaskPayloadUpdated.class) //
            .put("FutureTaskCreated", FutureTaskCreated.class) //
            .put("TaskCreated", TaskCreated.class) //
            .put("TaskTriggered", TaskTriggered.class) //
            .put("TaskStarted", TaskStarted.class) //
            .put("TaskRequeued", TaskRequeued.class) //
            .put("TaskClosed", TaskClosed.class) //
            .put("TaskArchived", TaskArchived.class) //
            .build();

    private final Map<String, Class<? extends PatchOperation>> supportedPatchOperations = new ImmutableMap.Builder<String, Class<? extends PatchOperation>>() //
            .put("Add", Add.class) //
            .put("Replace", Replace.class) //
            .put("Remove", Remove.class) //
            .build();

    private final Optional<CustomDefinedValueMapper> customValueMapper;

    public ObjectMapperFactory(final Optional<CustomDefinedValueMapper> customValueMapper)
    {
        checkArgument(customValueMapper != null, "Missing custom object mapper");
        //noinspection ConstantConditions
        this.customValueMapper = customValueMapper;
    }

    public ObjectMapper create()
    {
        final SimpleModule module = new SimpleModule("jacksonConfig", unknownVersion());
        module.addDeserializer(DateTime.class, new DateTimeJsonDeserializer());
        module.addDeserializer(CustomDefinedValue.class, new CustomDefinedValueJsonDeserializer());
        module.addDeserializer(Event.class, new EventJsonDeserializer());
        module.addDeserializer(PatchOperation.class, new PatchOperationJsonDeserializer());

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.registerModule(new GuavaModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    private class DateTimeJsonDeserializer extends JsonDeserializer<DateTime>
    {
        private final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
                .append(null, new DateTimeParser[] {DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").getParser(), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser()})
                .toFormatter();

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

    private class EventJsonDeserializer extends JsonDeserializer<Event>
    {
        @Override public Event deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException
        {
            final TreeNode node = jp.readValueAsTree();
            final String eventType = ((TextNode) node.get("type")).asText();
            if (supportedEvents.containsKey(eventType))
            {
                return jp.getCodec().treeToValue(node, supportedEvents.get(eventType));
            }
            else
            {
                return null;
            }
        }
    }

    private class PatchOperationJsonDeserializer extends JsonDeserializer<PatchOperation>
    {
        @Override public PatchOperation deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException
        {
            final TreeNode node = jp.readValueAsTree();
            final String operationType = ((TextNode) node.get("op")).asText();
            return jp.getCodec().treeToValue(node, supportedPatchOperations.get(operationType));
        }
    }
}
