/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 在XML中定义的Executor
 *
 * @author nomiky
 * @since 2023年12月22日 17时02分
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class XmlExecutor extends XmlEntity{

    public static final String TYPE_SQL = "sql";
    public static final String TYPE_BEAN = "bean";
    public static final String TYPE_DEFAULT = "default";

    public static final String ENGINE_DEFAULT = "DaoExecutor";
    public static final String ENGINE_GROOVY = "Groovy";
    public static final String ENGINE_JAVASCRIPT = "JavaScript";

    public static final String OPERATOR_SELECT = "select";
    public static final String OPERATOR_BATCH = "batch";
    public static final String OPERATOR_DELETE = "delete";
    public static final String OPERATOR_UPDATE = "update";
    public static final String OPERATOR_INSERT = "insert";

    /**
     * 默认类型的执行器使用DaoExecutor已经定义好的方法执行
     * SQL类型的执行器，会解析SQL语句执行
     */
    private String type;

    /**
     * SQL语句解析引擎：
     * Groovy：符合Groovy语法的SQL语句和参数拼装语句
     * JavaScript：符合JavaScript语法的SQL语句和参数拼装语句
     * DaoExecutor: 默认提供的执行器
     */
    private String engine;

    /**
     * 引用的属性映射，在返回数据和传参时将使用这个映射进行赋值
     */
    private String mapperRef;

    /**
     * 数据表的schema
     */
    private String schema;

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
    private String operator;

    /**
     * SQL语句定义
     */
    private String sqlDefinition;
}
