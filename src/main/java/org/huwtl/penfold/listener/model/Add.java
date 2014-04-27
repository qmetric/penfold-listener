package org.huwtl.penfold.listener.model;

public class Add extends PatchOperation
{
    public final Object value;

    public Add(final String type, final String path, final Object value)
    {
        super(type, path);
        this.value = value;
    }
}
