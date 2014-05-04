package org.huwtl.penfold.listener.domain;

public class ConflictException extends Exception
{
    public ConflictException()
    {
    }

    public ConflictException(final Throwable throwable)
    {
        super(throwable);
    }
}
