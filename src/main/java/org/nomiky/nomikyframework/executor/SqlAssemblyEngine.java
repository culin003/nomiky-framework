/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import cn.hutool.core.lang.Pair;

import javax.script.ScriptException;

/**
 * <pre>
 *     SQL语句拼装引擎
 *     Groovy：符合Groovy语法的SQL语句和参数拼装语句
 *     JavaScript：符合JavaScript语法的SQL语句和参数拼装语句
 *
 *     引擎环境中自动注入变量：
 *     header：从请求头中获取指定字段的参数值
 *     param：从请求URL参数中获取指定字段的参数值
 *     bodyString：使用request body字符串作为参数值
 *     bodyJson：从request body获取JSON对象作为参数值
 *     path：从请求路径中获取指定字段的参数值
 *     parent: 使用上一级Executor的结果作为参数
 *
 *     SQL语句拼装结果变量：
 *     sqlResult
 *
 *     SQL语句参数变量：
 *     sqlParams
 * </pre>
 *
 * @author nomiky
 * @since 2023年12月27日 15时48分
 */
public interface SqlAssemblyEngine {

    String SQL_RESULT = "sqlResult";
    String SQL_PARAMS = "sqlParams";

    /**
     * 从引擎语句中解析出SQL语句和参数
     *
     * @param engineScript 引擎语句
     * @return SQL语句和参数
     */
    Pair<String, Object[]> parseSql(String engineScript) throws ScriptException;

}
