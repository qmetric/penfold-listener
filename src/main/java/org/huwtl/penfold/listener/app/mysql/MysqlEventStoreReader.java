package org.huwtl.penfold.listener.app.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.huwtl.penfold.listener.domain.EventStoreReader;
import org.huwtl.penfold.listener.domain.model.Event;
import org.huwtl.penfold.listener.domain.model.EventRecord;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlEventStoreReader implements EventStoreReader
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlEventStoreReader.class);

    private final DBI dbi;

    private final ObjectMapper objectMapper;

    MysqlEventStoreReader(final DataSource dataSource, final ObjectMapper objectMapper)
    {
        dbi = new DBI(dataSource);
        this.objectMapper = objectMapper;
    }

    @Override public Optional<EventSequenceId> retrieveLastEventId()
    {
        return dbi.withHandle(new HandleCallback<Optional<EventSequenceId>>()
        {
            public Optional<EventSequenceId> withHandle(final Handle handle) throws Exception
            {
                return Optional.fromNullable(handle.createQuery("SELECT id FROM events ORDER BY id DESC LIMIT 1") //
                                                     .map(new EventSequenceIdMapper()) //
                                                     .first() //
                );
            }
        });
    }

    @Override public Optional<EventRecord> retrieveBy(final EventSequenceId id)
    {
        return dbi.withHandle(new HandleCallback<Optional<EventRecord>>()
        {
            public Optional<EventRecord> withHandle(final Handle handle) throws Exception
            {
                return Optional.fromNullable(handle.createQuery("SELECT id, data FROM events WHERE id = :id") //
                                                     .bind("id", id.value) //
                                                     .map(new EventRecordMapper()) //
                                                     .first() //
                );
            }
        });
    }

    private class EventRecordMapper implements ResultSetMapper<EventRecord>
    {
        @Override public EventRecord map(final int row, final ResultSet resultSet, final StatementContext statementContext) throws SQLException
        {
            final Long eventId = resultSet.getLong(1);
            final String rawEventData = resultSet.getString(2);

            return new EventRecord(new EventSequenceId(eventId), parseEvent(rawEventData));
        }

        private Event parseEvent(final String rawEventData)
        {
            try
            {
                return objectMapper.readValue(rawEventData, Event.class);
            }
            catch (IOException e)
            {
                LOGGER.error(String.format("failed to parse event %s", rawEventData), e);
                throw new RuntimeException(e);
            }
        }
    }

    private static class EventSequenceIdMapper implements ResultSetMapper<EventSequenceId>
    {
        @Override public EventSequenceId map(final int row, final ResultSet resultSet, final StatementContext statementContext) throws SQLException
        {
            return new EventSequenceId(resultSet.getLong(1));
        }
    }
}
