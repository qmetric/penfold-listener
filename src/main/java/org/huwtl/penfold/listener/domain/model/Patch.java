package org.huwtl.penfold.listener.domain.model;

import java.util.List;

public class Patch
{
    public final List<PatchOperation> operations;

    private Patch()
    {
        operations = null;
    }

    public Patch(final List<PatchOperation> operations)
    {
        this.operations = operations;
    }
}
