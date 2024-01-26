/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nomiky
 * @since 2024年01月23日 17时49分
 */
public class FieldValueAutoGenaratorHelper {

    public static final String INSERT = "INSERT";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";


    private static final Map<String, FieldValueAutoGenerator> FIELD_VALUE_AUTO_GENARATOR_MAP = new HashMap<>();

    public static void register(String operation, FieldValueAutoGenerator generator) {
        if (StrUtil.isEmpty(operation) || null == generator) {
            return;
        }

        FIELD_VALUE_AUTO_GENARATOR_MAP.put(operation, generator);
    }

    public static void autoGenerate(String operation, Map<String, Object> valueMap) {
        if (StrUtil.isEmpty(operation)
                || FIELD_VALUE_AUTO_GENARATOR_MAP.isEmpty()
                || !FIELD_VALUE_AUTO_GENARATOR_MAP.containsKey(operation)) {
            return;
        }

        FIELD_VALUE_AUTO_GENARATOR_MAP.get(operation).generate(valueMap);
    }
}
