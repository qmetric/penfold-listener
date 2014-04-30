package org.huwtl.penfold.listener.app;

import com.google.common.base.Optional;
import org.huwtl.penfold.listener.domain.model.Event;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlEventStore
{
    private final DBI dbi;

    MysqlEventStore(final MysqlEventStoreConfiguration configuration, final DataSource dataSource)
    {
        dbi = new DBI(dataSource);
    }

    private Optional<Event> retrieveBy()
    {
        return dbi.withHandle(new HandleCallback<Optional<Event>>()
        {
            public Optional<Event> withHandle(final Handle handle) throws Exception
            {
                return Optional.fromNullable(handle.createQuery("SELECT id, failures_count FROM events WHERE id = :id") //
                                                     .bind("id", "abc") //
                                                     .map(new EventMapper()) //
                                                     .first() //
                );
            }
        });
    }

    private static class EventMapper implements ResultSetMapper<Event>
    {
        @Override public Event map(final int row, final ResultSet resultSet, final StatementContext statementContext) throws SQLException
        {
            final String id = resultSet.getString("id");

            final int num = resultSet.getInt("num");

            return null;
        }
    }
}
