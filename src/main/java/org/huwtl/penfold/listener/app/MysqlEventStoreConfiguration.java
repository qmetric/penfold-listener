package org.huwtl.penfold.listener.app;

public class MysqlEventStoreConfiguration
{
    private static final String DEFAULT_MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    public final String driver;

    public final String url;

    public final String username;

    public final String password;

    public MysqlEventStoreConfiguration(final String url, final String username, final String password)
    {
        this(DEFAULT_MYSQL_DRIVER, url, username, password);
    }

    public MysqlEventStoreConfiguration(final String driver, final String url, final String username, final String password)
    {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }
}
