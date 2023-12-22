/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import lombok.Data;

/**
 * 在XML中定义的Executor
 *
 * @author nomiky
 * @since 2023年12月22日 17时02分
 */
@Data
public class XmlExecutor {

    /**
     * 引用的属性映射，在返回数据和传参时将使用这个映射进行赋值
     */
    private String mapperRef;

    /**
     * 引用某个表的Executor,格式为：tableName.methodName，调用此tableName对应的Executor的methodName方法
     * 此属性与xmlSqlDefinition互斥
     */
    private String ref;

    /**
     * 方法的参数来源，这个参数从controller中获取，获取方式有：header、param、bodyString、bodyJson
     * header：从请求头中获取指定字段的参数值
     * param：从请求URL参数中获取指定字段的参数值
     * bodyString：使用request body字符串作为参数值
     * bodyJson：从request body获取JSON对象作为参数值
     * path：从请求路径中获取指定字段的参数值
     * parent: 使用上一级Executor的结果作为参数
     */
    private String params;

    /**
     * SQL语句定义，和ref属性互斥
     */
    private XmlSqlDefinition xmlSqlDefinition;
}
