package org.huwtl.penfold.listener.domain.model;

public class Replace extends PatchOperation
{
    public final CustomDefinedValue value;

    private Replace()
    {
        value = null;
    }

    public Replace(final String type, final String path, final CustomDefinedValue value)
    {
        super(type, path);
        this.value = value;
    }
}
