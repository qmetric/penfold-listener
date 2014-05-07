package org.huwtl.penfold.listener.app.health;

import com.codahale.metrics.health.HealthCheck;
import org.huwtl.penfold.listener.app.mysql.ConnectivityAware;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;

public class ConnectivityHealthCheck extends HealthCheck
{
    private final ConnectivityAware connectivityAware;

    public ConnectivityHealthCheck(final ConnectivityAware connectivityAware)
    {
        this.connectivityAware = connectivityAware;
    }

    @Override protected HealthCheck.Result check() throws Exception
    {
        try
        {
            connectivityAware.checkConnectivity();

            return healthy("connectivity is healthy");
        }
        catch (final Exception exception)
        {
            return unhealthy(exception);
        }
    }
}
