package org.huwtl.penfold.listener.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true, property = "op")
@JsonSubTypes({
                  @JsonSubTypes.Type(value = Add.class, name = "Add"),
                  @JsonSubTypes.Type(value = Remove.class, name = "Remove"),
                  @JsonSubTypes.Type(value = Replace.class, name = "Replace")
              })
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
