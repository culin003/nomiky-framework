/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.bean;

import cn.hutool.core.util.StrUtil;
import org.nomiky.nomikyframework.entity.Page;
import org.nomiky.nomikyframework.entity.TableDefinition;
import org.nomiky.nomikyframework.exception.ExecutorException;
import org.nomiky.nomikyframework.executor.DaoExecutor;
import org.nomiky.nomikyframework.util.Checker;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.nomiky.nomikyframework.enums.ExecutorEnum.*;

/**
 * 生成表的Executor
 *
 * @author nomiky
 * @since 2023年12月21日 18时24分
 */
public class DaoExecutorBeanProcessor {

    /**
     * 创建指定表的Executor
     *
     * @param tableDefinition 表
     * @return 指定表的Executor
     */
    public DaoExecutor createSpecifyExecutor(final TableDefinition tableDefinition, final JdbcTemplate jdbcTemplate) {
        return new DaoExecutor() {
            public String getTableName() {
                Checker.checkEmpty(TABLE_NAME_EMPTY, tableDefinition.getName());
                return tableDefinition.getName();
            }

            public int insert(final Map<String, Object> valuesMap) {
                String primaryKey = tableDefinition.getPrimaryKey();
                Checker.checkEmpty(TABLE_EXPLAIN_ERROR, primaryKey);
                StringBuilder sqlBuilder = new StringBuilder();
                final LinkedHashSet<String> columnSet = new LinkedHashSet<>(valuesMap.keySet());
                columnSet.remove(primaryKey);

                sqlBuilder.append("INSERT INTO ")
                        .append(getTableName())
                        .append('(')
                        .append(primaryKey)
                        .append(',')
                        .append(StrUtil.join(",", columnSet))
                        .append(')')
                        .append(" VALUES (");
                for (int i = 0; i < columnSet.size(); i++) {
                    sqlBuilder.append((i == columnSet.size() - 1) ? "?" : "?, ");
                }

                sqlBuilder.append(')');
                return jdbcTemplate.update(sqlBuilder.toString(), ps -> {
                    ps.setObject(1, tableDefinition.generateId());
                    int index = 2;
                    for (String tableColumn : columnSet) {
                        if (valuesMap.containsKey(tableColumn)) {
                            ps.setObject(index++, valuesMap.get(tableColumn));
                        }
                    }
                });
            }

            public int deleteById(Serializable id) {
                String primaryKey = tableDefinition.getPrimaryKey();
                Checker.checkEmpty(TABLE_EXPLAIN_ERROR, primaryKey);
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("DELETE FROM ")
                        .append(tableDefinition.getName())
                        .append(" WHERE ")
                        .append(primaryKey)
                        .append(" = ?");
                return jdbcTemplate.update(sqlBuilder.toString(), id);
            }

            public int updateById(Map<String, Object> valuesMap) {
                String primaryKey = tableDefinition.getPrimaryKey();
                Checker.checkEmpty(TABLE_EXPLAIN_ERROR, primaryKey);
                if (!valuesMap.containsKey(primaryKey)) {
                    throw new ExecutorException("Primary key value is unset！");
                }

                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("UPDATE ")
                        .append(tableDefinition.getName())
                        .append(" SET");
                LinkedHashMap<String, Object> finalValueMap = new LinkedHashMap<>(valuesMap);
                finalValueMap.forEach((k, v) -> {
                    if (!k.equalsIgnoreCase(primaryKey)) {
                        sqlBuilder.append(' ').append(k).append(" = ?,");
                    }
                });

                sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                sqlBuilder.append(" WHERE ").append(primaryKey).append(" = ?");

                return jdbcTemplate.update(sqlBuilder.toString(), ps -> {
                    int index = 1;
                    for (Map.Entry<String, Object> entry : finalValueMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (!key.equalsIgnoreCase(primaryKey)) {
                            ps.setObject(index++, value);
                        }
                    }

                    ps.setObject(index, valuesMap.get(primaryKey));
                });
            }

            public List<Map<String, Object>> select(Map<String, Object> valuesMap) {
                return null;
            }

            public Map<String, Object> selectOne(Map<String, Object> valuesMap) {
                return null;
            }

            public Boolean exist(Map<String, Object> valuesMap) {
                return null;
            }

            public Long count(Map<String, Object> valuesMap) {
                return null;
            }

            public Page selectPage(Map<String, Object> valuesMap) {
                return null;
            }
        };
    }

}
