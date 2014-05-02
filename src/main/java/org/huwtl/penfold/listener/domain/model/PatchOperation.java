package org.huwtl.penfold.listener.domain.model;

public abstract class PatchOperation
{
    public final String op;

    public final String path;

    protected PatchOperation() {
        op = null;
        path = null;
    }

    protected PatchOperation(final String op, final String path)
    {
        this.op = op;
        this.path = path;
    }
}
