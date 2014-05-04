package org.huwtl.penfold.listener.domain;

import com.google.common.reflect.TypeToken;

public interface CustomDefinedValueMapper
{
    public <T> T map(String raw, final TypeToken<T> type);
}
