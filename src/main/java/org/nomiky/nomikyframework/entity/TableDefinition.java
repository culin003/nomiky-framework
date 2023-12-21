/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import cn.hutool.core.util.IdUtil;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

/**
 * 表
 *
 * @author nomiky
 * @since 2023年12月21日 18时41分
 */
@Data
public class TableDefinition {

    public static final String UUID_GENERATOR = "UUID";
    public static final String SNOWFLAKE_GENERATOR = "SNOWFLAKE";
    public static final String NANOID_GENERATOR = "NANOID";


    /**
     * 表名称
     */
    private String name;

    /**
     * 表主键字段
     */
    private String primaryKey;

    /**
     * ID产生器，支持：UUID、SNOWFLAKE、NANOID
     */
    private String primaryKeyGenerator = SNOWFLAKE_GENERATOR;

    /**
     * 表字段及其数据类型
     */
    private LinkedHashMap<String, Type> columns;

    /**
     * 根据ID生成策略生成ID
     *
     * @return ID
     */
    public Object generateId() {
        return switch (primaryKeyGenerator) {
            case UUID_GENERATOR -> IdUtil.fastSimpleUUID();
            case NANOID_GENERATOR -> IdUtil.nanoId();
            default -> IdUtil.getSnowflakeNextId();
        };
    }
}
