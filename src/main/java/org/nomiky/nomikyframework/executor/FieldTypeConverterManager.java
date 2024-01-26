/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nomiky
 * @since 2024年01月23日 17时10分
 */
public class FieldTypeConverterManager {

    private final static Map<Type, FieldTypeConverter> CONVERTER_MAP = new HashMap<>();

    static {
        register(Date.class, value -> null == value ? null : DateUtil.format((Date) value, DatePattern.NORM_DATETIME_PATTERN));
        register(LocalDateTime.class, value -> null == value ? null : DateUtil.format((LocalDateTime) value, DatePattern.NORM_DATETIME_PATTERN));
    }

    public static void register(Type type, FieldTypeConverter converter) {
        if (null == type || null == converter) {
            return;
        }

        CONVERTER_MAP.put(type, converter);
    }

    public static Object convert(Object value) {
        if (value == null) {
            return new HashMap<>(0);
        }

        if (CONVERTER_MAP.size() == 0) {
            return value;
        }

        if (!(value instanceof Map)) {
            Type type = value.getClass();
            if (CONVERTER_MAP.containsKey(type)) {
                return CONVERTER_MAP.get(type).convert(value);
            } else {
                return value;
            }
        }

        Map<String, Object> temp = (Map) value;
        Map<String, Object> valueResult = new HashMap<>(temp.size());
        temp.forEach((k, v) -> {
            if (v != null) {
                Type type = v.getClass();
                if (CONVERTER_MAP.containsKey(type)) {
                    v = CONVERTER_MAP.get(type).convert(v);
                }
            }

            valueResult.put(k, v);
        });

        temp.clear();
        return valueResult;
    }
}
