package org.huwtl.penfold.listener.model;

import java.util.List;

public class Patch
{
    public final List<PatchOperation> operations;

    public Patch(final List<PatchOperation> operations)
    {
        this.operations = operations;
    }
}
