/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import lombok.Data;

/**
 * 在XML中定义的table
 *
 * @author nomiky
 * @since 2023年12月22日 09时53分
 */
@Data
public class XmlTable {

    private String schema;

    /**
     * 表名
     */
    private String name;

    /**
     * ID产生器，支持：UUID、SNOWFLAKE、NANOID
     */
    private String primaryKeyGenerator = TableDefinition.SNOWFLAKE_GENERATOR;
}
