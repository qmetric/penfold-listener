package org.huwtl.penfold.listener.app.mysql;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;

import java.beans.PropertyVetoException;

public class MysqlDataSourceFactory
{
    public static DataSource create(final MysqlEventStoreConfiguration configuration)
    {
        final ComboPooledDataSource dataSource = new ComboPooledDataSource(true);

        try
        {
            dataSource.setDriverClass(configuration.driver);
        }
        catch (PropertyVetoException e)
        {
            throw new RuntimeException(e);
        }

        dataSource.setJdbcUrl(configuration.url);
        dataSource.setUser(configuration.username);
        dataSource.setPassword(configuration.password);
        dataSource.setPreferredTestQuery("select 1");
        dataSource.setIdleConnectionTestPeriod(60);

        return dataSource;
    }
}
