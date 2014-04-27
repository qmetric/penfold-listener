package org.huwtl.penfold.listener.model;

import java.util.Map;

public class Payload
{
    public final Map<String, Object> content;

    public Payload(final Map<String, Object> content)
    {
        this.content = content;
    }
}
