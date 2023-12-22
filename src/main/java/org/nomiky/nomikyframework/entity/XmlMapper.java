/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import lombok.Data;

/**
 * 在XML中定义的属性名称映射
 *
 * @author nomiky
 * @since 2023年12月22日 16时58分
 */
@Data
public class XmlMapper {

    /**
     * java对象属性名称
     */
    private String attrName;

    /**
     * 表字段名称
     */
    private String columnName;
}
