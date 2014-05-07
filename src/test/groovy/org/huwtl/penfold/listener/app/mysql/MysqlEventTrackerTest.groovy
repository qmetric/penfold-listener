package org.huwtl.penfold.listener.app.mysql

import com.google.common.base.Optional
import com.mchange.v2.c3p0.ComboPooledDataSource
import groovy.sql.Sql
import org.huwtl.penfold.listener.app.DateTimeSource
import org.huwtl.penfold.listener.domain.ConflictException
import org.huwtl.penfold.listener.domain.model.EventSequenceId
import org.huwtl.penfold.listener.domain.model.EventTrackingRecord
import org.joda.time.DateTime
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource

class MysqlEventTrackerTest extends Specification {

    @Shared DateTime currentDate = new DateTime(2014, 2, 14, 12, 0, 0, 0)

    @Shared DateTimeSource dateTimeSource = Mock(DateTimeSource)

    @Shared MysqlEventTracker eventTracker

    @Shared Sql sql

    def setupSpec()
    {
        dateTimeSource.now() >> currentDate
        def dataSource = initDataSource()
        eventTracker = new MysqlEventTracker(initDataSource(), "tracker1", dateTimeSource)
        sql = new Sql(dataSource)
    }

    def cleanup()
    {
        sql.execute("DELETE FROM event_trackers")
    }

    def "should know when connection established"()
    {
        when:
        eventTracker.checkConnectivity()

        then:
        notThrown(RuntimeException)
    }

    def "should know when no tracking history"()
    {
        expect:
        eventTracker.lastTracked() == Optional.absent()
    }

    def "should know most recent event tracker record"()
    {
        given:
        eventTracker.markAsStarted(EventSequenceId.first())

        expect:
        eventTracker.lastTracked() == Optional.of(new EventTrackingRecord(EventSequenceId.first(), Optional.of(currentDate), Optional.absent()))
    }

    def "should prevent concurrent handling of the same event"()
    {
        when:
        2.times { eventTracker.markAsStarted(EventSequenceId.first()) }

        then:
        thrown(ConflictException)
    }

    def "should prevent handling of an already handled event"()
    {
        given:
        eventTracker.markAsStarted(EventSequenceId.first())
        eventTracker.markAsCompleted(EventSequenceId.first())

        when:
        eventTracker.markAsStarted(EventSequenceId.first())

        then:
        thrown(ConflictException)
    }

    def "should track event first event as being started by handler"()
    {
        when:
        eventTracker.markAsStarted(EventSequenceId.first())

        then:
        final lastTracked = eventTracker.lastTracked()
        lastTracked.get().isAlreadyStarted()
        lastTracked == Optional.of(new EventTrackingRecord(EventSequenceId.first(), Optional.of(currentDate), Optional.absent()))
    }

    def "should track next event as being started following a previous completed event"()
    {
        given:
        eventTracker.markAsStarted(EventSequenceId.first())
        eventTracker.markAsCompleted(EventSequenceId.first())

        when:
        eventTracker.markAsStarted(EventSequenceId.first().next())

        then:
        final lastTracked = eventTracker.lastTracked()
        lastTracked.get().isAlreadyStarted()
        lastTracked == Optional.of(new EventTrackingRecord(EventSequenceId.first().next(), Optional.of(currentDate), Optional.absent()))
    }

    def "should track next event again after being unstarted"()
    {
        given:
        eventTracker.markAsStarted(EventSequenceId.first())
        eventTracker.markAsUnstarted(EventSequenceId.first())

        when:
        eventTracker.markAsStarted(EventSequenceId.first())

        then:
        final lastTracked = eventTracker.lastTracked()
        lastTracked.get().isAlreadyStarted()
        lastTracked == Optional.of(new EventTrackingRecord(EventSequenceId.first(), Optional.of(currentDate), Optional.absent()))
    }

    def "should track event as being completed by handler"()
    {
        given:
        eventTracker.markAsStarted(EventSequenceId.first())

        when:
        eventTracker.markAsCompleted(EventSequenceId.first())

        then:
        final lastTracked = eventTracker.lastTracked()
        lastTracked.get().isNotAlreadyStarted()
        lastTracked == Optional.of(new EventTrackingRecord(EventSequenceId.first(), Optional.of(currentDate), Optional.of(currentDate)))
    }

    private static DataSource initDataSource()
    {
        final DataSource datasource = new ComboPooledDataSource()
        datasource.setDriverClass('org.hsqldb.jdbcDriver')
        datasource.setJdbcUrl("jdbc:hsqldb:mem:event_trackers;sql.syntax_mys=true")
        datasource.setUser('sa')
        datasource.setPassword('')
        datasource
    }
}
