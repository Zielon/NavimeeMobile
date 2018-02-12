package org.pl.android.drively.util;

import java.lang.reflect.Field;

public class ReflectionUtil {
    public static <T>String nameof(Class<T> type, String fieldName) throws NoSuchFieldException {
        Field field = type.getDeclaredField(fieldName);
        return field.getName();
    }
}
