package org.huwtl.penfold.listener.domain.model;

public class Add extends PatchOperation
{
    public final CustomDefinedValue value;

    private Add()
    {
        value = null;
    }

    public Add(final String type, final String path, final CustomDefinedValue value)
    {
        super(type, path);
        this.value = value;
    }
}
