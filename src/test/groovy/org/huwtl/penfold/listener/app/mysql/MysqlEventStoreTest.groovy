package org.huwtl.penfold.listener.app.mysql
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import groovy.sql.Sql
import org.hsqldb.jdbc.JDBCDataSource
import org.huwtl.penfold.listener.domain.EventStore
import org.huwtl.penfold.listener.domain.model.Event
import org.huwtl.penfold.listener.domain.model.EventRecord
import org.huwtl.penfold.listener.domain.model.EventSequenceId
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource

class MysqlEventStoreTest extends Specification {

    @Shared Sql sql

    @Shared EventStore eventStore

    @Shared Event event = Mock(Event)

    def setupSpec()
    {
        final dataSource = initDataSource()
        final objectMapper = Mock(ObjectMapper)
        eventStore = new MysqlEventStore(dataSource, objectMapper)
        objectMapper.readValue(_ as String, Event.class) >> event

        initSchema(dataSource)
    }

    def cleanup()
    {
        sql.execute('DELETE FROM events')
    }

    def "should know when connection established"()
    {
        when:
        eventStore.checkConnectivity()

        then:
        notThrown(RuntimeException)
    }

    def "should known when no events"()
    {
        expect:
        eventStore.retrieveLastEventId() == Optional.absent()
    }

    def "should known id of most recent event"()
    {
        given:
        insertEvent(1, "{}")
        insertEvent(2, "{}")

        expect:
        eventStore.retrieveLastEventId() == Optional.of(new EventSequenceId(2))
    }

    def "should retrieve event by id"()
    {
        given:
        insertEvent(1, "{}")
        insertEvent(2, "{}")

        expect:
        eventStore.retrieveBy(new EventSequenceId(2)) == Optional.of(new EventRecord(new EventSequenceId(2), event))
        !eventStore.retrieveBy(new EventSequenceId(3)).isPresent()
    }

    private static DataSource initDataSource()
    {
        final DataSource datasource = new JDBCDataSource()
        datasource.setUrl("jdbc:hsqldb:mem:events;sql.syntax_mys=true")
        datasource
    }

    private def initSchema(final DataSource dataSource)
    {
        sql = new Sql(dataSource)
        sql.execute("""
          CREATE TABLE events
          (
            id INT NOT NULL,
            data TEXT NOT NULL,
            PRIMARY KEY (id)
          );
        """)
    }

    def insertEvent(final id, final data)
    {
        sql.execute("insert into events (id, data) values (?, ?)", [id, data])
    }
}
