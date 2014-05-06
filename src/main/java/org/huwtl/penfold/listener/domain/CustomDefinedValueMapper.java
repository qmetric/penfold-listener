package org.huwtl.penfold.listener.domain;

import com.google.common.reflect.TypeToken;

public interface CustomDefinedValueMapper
{
    <T> T map(String raw, TypeToken<T> type);
}
