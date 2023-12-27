/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * <pre>
 *    基于Groovy语法的SQL语句拼装引擎
 *    需要引入Groovy引擎包:
 *    <dependency>
 *        <groupId>org.apache.groovy</groupId>
 *        <artifactId>groovy</artifactId>
 *        <version>${groovy.version}</version>
 *    </dependency>
 * </pre>
 *
 * @author nomiky
 * @since 2023年12月27日 15时54分
 */
public class GroovySqlAssemblyEngine implements SqlAssemblyEngine {

    @Override
    public Pair<String, Object[]> parseSql(String engineScript) {
        GroovyShell groovyShell = new GroovyShell();
        Script script = groovyShell.parse(engineScript);
        script.setProperty(SQL_RESULT, StrUtil.EMPTY);
        script.setProperty(SQL_PARAMS, new Object[0]);
        script.run();
        return Pair.of((String) script.getProperty(SQL_RESULT), (Object[]) script.getProperty(SQL_PARAMS));
    }
}
