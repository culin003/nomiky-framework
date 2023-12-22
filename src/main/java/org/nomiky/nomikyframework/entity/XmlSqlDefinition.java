/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import lombok.Data;

/**
 * 在XML中定义的SQL语句
 *
 * @author nomiky
 * @since 2023年12月22日 17时13分
 */
@Data
public class XmlSqlDefinition {

    /**
     * SQL操作：select、update、delete、insert
     * 不支持DML、DDL
     */
    private String operator;

    private String sql;

}
