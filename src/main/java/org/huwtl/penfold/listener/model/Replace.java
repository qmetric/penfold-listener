package org.huwtl.penfold.listener.model;

public class Replace extends PatchOperation
{
    public final Object value;

    public Replace(final String type, final String path, final Object value)
    {
        super(type, path);
        this.value = value;
    }
}
