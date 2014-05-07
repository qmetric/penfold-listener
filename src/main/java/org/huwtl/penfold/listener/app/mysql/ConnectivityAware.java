package org.huwtl.penfold.listener.app.mysql;

import org.huwtl.penfold.listener.domain.ConnectivityException;

public interface ConnectivityAware
{
    void checkConnectivity() throws ConnectivityException;
}
