package org.huwtl.penfold.listener.model;

public abstract class PatchOperation
{
    public final String type;

    public final String path;

    protected PatchOperation(final String type, final String path)
    {
        this.type = type;
        this.path = path;
    }
}
