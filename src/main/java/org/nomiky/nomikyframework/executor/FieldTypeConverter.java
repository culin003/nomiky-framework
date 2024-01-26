/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

/**
 * 属性类型转换器，将从数据库中查询到的一些属性转换成前端可以理解的值
 *
 * @author nomiky
 * @since 2024年01月23日 17时08分
 */
public interface FieldTypeConverter {

    Object convert(Object value);

}
