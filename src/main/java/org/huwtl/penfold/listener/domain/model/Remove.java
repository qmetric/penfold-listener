package org.huwtl.penfold.listener.domain.model;

public class Remove extends PatchOperation
{
    private Remove()
    {
    }

    public Remove(final String type, final String path)
    {
        super(type, path);
    }
}
