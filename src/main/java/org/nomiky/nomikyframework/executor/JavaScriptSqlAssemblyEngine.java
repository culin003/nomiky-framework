/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;

import javax.script.*;

/**
 * <pre>
 *    基于JavaScript语法的SQL语句拼装引擎
 *    需要引入JavaScript引擎包:
 *    <dependency>
 *        <groupId>org.openjdk.nashorn</groupId>
 *        <artifactId>nashorn-core</artifactId>
 *        <version>${nashorn.version}</version>
 *    </dependency>
 * </pre>
 *
 * @author nomiky
 * @since 2023年12月27日 15时54分
 */
public class JavaScriptSqlAssemblyEngine implements SqlAssemblyEngine {

    @Override
    public Pair<String, Object[]> parseSql(String engineScript) throws ScriptException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine jsEngine = engineManager.getEngineByName("JavaScript");
        Bindings bindings = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put(SQL_RESULT, StrUtil.EMPTY);
        bindings.put(SQL_PARAMS, new Object[0]);
        jsEngine.eval(engineScript, bindings);
        return Pair.of((String) bindings.get(SQL_RESULT), (Object[]) bindings.get(SQL_PARAMS));
    }
}
