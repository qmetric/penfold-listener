package org.huwtl.penfold.listener.app.mysql;

import com.google.common.base.Optional;
import com.googlecode.flyway.core.Flyway;
import org.huwtl.penfold.listener.app.DateTimeSource;
import org.huwtl.penfold.listener.domain.ConflictException;
import org.huwtl.penfold.listener.domain.ConnectivityException;
import org.huwtl.penfold.listener.domain.EventTracker;
import org.huwtl.penfold.listener.domain.model.EventSequenceId;
import org.huwtl.penfold.listener.domain.model.EventTrackingRecord;
import org.huwtl.penfold.listener.domain.model.TrackingStatus;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MysqlEventTracker implements EventTracker
{
    private static final Timestamp NULL_DATE = null;

    private final DBI dbi;

    private final String trackerId;

    private final DateTimeSource dateTimeSource;

    public MysqlEventTracker(final DataSource dataSource, final String trackerId)
    {
        this(dataSource, trackerId, new DateTimeSource());
    }

    public MysqlEventTracker(final DataSource dataSource, final String trackerId, final DateTimeSource dateTimeSource)
    {
        this.trackerId = trackerId;
        this.dateTimeSource = dateTimeSource;
        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("database/migrations/penfold_listener");
        flyway.migrate();
        dbi = new DBI(dataSource);
    }

    @Override
    public void checkConnectivity() throws ConnectivityException
    {
        dbi.withHandle(new HandleCallback<Boolean>()
        {
            public Boolean withHandle(final Handle handle) throws Exception
            {
                handle.createQuery("SELECT 1").first();
                return true;
            }
        });
    }

    @Override
    public Optional<EventTrackingRecord> lastTracked()
    {
        return dbi.withHandle(new HandleCallback<Optional<EventTrackingRecord>>()
        {
            public Optional<EventTrackingRecord> withHandle(final Handle handle) throws Exception
            {
                return Optional.fromNullable(handle.createQuery("SELECT event_id, status, seen FROM event_trackers WHERE id = :trackerId") //
                        .bind("trackerId", trackerId)
                        .map(new EventTrackingRecordMapper()) //
                        .first() //
                );
            }
        });
    }

    @Override
    public void markAsStarted(final EventSequenceId id) throws ConflictException
    {
        final Timestamp now = new Timestamp(dateTimeSource.now().getMillis());

        if (lastTracked().isPresent())
        {
            final Integer rowsUpdated = dbi.withHandle(new HandleCallback<Integer>()
            {
                public Integer withHandle(final Handle handle) throws Exception
                {
                    return handle.createStatement("UPDATE event_trackers SET status = :status, event_id = :eventId, seen = :seen, started = :started, completed = :completed WHERE id = :id AND ((event_id = :eventId AND status = 'UNSTARTED') OR (event_id < :eventId AND status = 'COMPLETED'))") //
                            .bind("id", trackerId) //
                            .bind("status", TrackingStatus.STARTED.name()) //
                            .bind("eventId", id.value) //
                            .bind("seen", now) //
                            .bind("started", now) //
                            .bind("completed", NULL_DATE) //
                            .execute();
                }
            });

            if (rowsUpdated == 0)
            {
                throw new ConflictException();
            }
        }
        else
        {
            dbi.withHandle(new HandleCallback<Integer>()
            {
                public Integer withHandle(final Handle handle) throws Exception
                {
                    return handle.createStatement("INSERT INTO event_trackers (id, status, event_id, seen, started) VALUES (:id, :status, :eventId, :seen, :started)") //
                            .bind("id", trackerId) //
                            .bind("status", TrackingStatus.STARTED.name()) //
                            .bind("eventId", id.value) //
                            .bind("seen", now) //
                            .bind("started", now) //
                            .execute();
                }
            });
        }
    }

    @Override
    public void markAsUnstarted(final EventSequenceId id)
    {
        final Timestamp now = new Timestamp(dateTimeSource.now().getMillis());

        dbi.withHandle(new HandleCallback<Integer>()
        {
            public Integer withHandle(final Handle handle) throws Exception
            {
                return handle.createStatement("UPDATE event_trackers SET status = :status, seen = :seen, started = :started, completed = :completed WHERE id = :id AND event_id = :eventId") //
                        .bind("id", trackerId) //
                        .bind("status", TrackingStatus.UNSTARTED.name()) //
                        .bind("eventId", id.value) //
                        .bind("seen", now) //
                        .bind("started", NULL_DATE) //
                        .bind("completed", NULL_DATE) //
                        .execute();
            }
        });
    }

    @Override
    public void markAsCompleted(final EventSequenceId id)
    {
        final Timestamp now = new Timestamp(dateTimeSource.now().getMillis());

        dbi.withHandle(new HandleCallback<Integer>()
        {
            public Integer withHandle(final Handle handle) throws Exception
            {

                return handle.createStatement("UPDATE event_trackers SET status = :status, seen = :seen, completed = :completed WHERE id = :id AND event_id = :eventId") //
                        .bind("id", trackerId) //
                        .bind("status", TrackingStatus.COMPLETED.name()) //
                        .bind("eventId", id.value) //
                        .bind("seen", now) //
                        .bind("completed", new Timestamp(dateTimeSource.now().getMillis())) //
                        .execute();
            }
        });
    }

    private static class EventTrackingRecordMapper implements ResultSetMapper<EventTrackingRecord>
    {
        @Override
        public EventTrackingRecord map(final int row, final ResultSet resultSet, final StatementContext statementContext) throws SQLException
        {
            final EventSequenceId id = new EventSequenceId(resultSet.getLong(1));
            final TrackingStatus status = TrackingStatus.valueOf(resultSet.getString(2));
            final DateTime lastModified = new DateTime(resultSet.getTimestamp(3).getTime());

            return new EventTrackingRecord(id, status, lastModified);
        }
    }
}
