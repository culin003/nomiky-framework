/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.nomiky.nomikyframework.constant.DaoConstants;
import org.nomiky.nomikyframework.entity.Page;
import org.nomiky.nomikyframework.entity.TableDefinition;
import org.nomiky.nomikyframework.exception.ExecutorException;
import org.nomiky.nomikyframework.executor.DaoExecutor;
import org.nomiky.nomikyframework.util.Checker;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                Pair<String, Object[]> parseResult = parseSelectSql(valuesMap, false, false);
                return jdbcTemplate.query(parseResult.getKey(), (rs, rowNum) -> {
                    Map<String, Object> resultMap = new HashMap<>();
                    for (String column : tableDefinition.getColumns().keySet()) {
                        resultMap.put(column, rs.getObject(column));
                    }
                    return resultMap;
                }, parseResult.getValue());
            }

            public Map<String, Object> selectOne(Map<String, Object> valuesMap) {
                Pair<String, Object[]> parseResult = parseSelectSql(valuesMap, false, true);
                List<Map<String, Object>> result = jdbcTemplate.query(parseResult.getKey(), (rs, rowNum) -> {
                    Map<String, Object> resultMap = new HashMap<>();
                    for (String column : tableDefinition.getColumns().keySet()) {
                        resultMap.put(column, rs.getObject(column));
                    }
                    return resultMap;
                }, parseResult.getValue());
                return CollUtil.isEmpty(result) ? new HashMap<>(0) : result.get(0);
            }

            public Boolean exist(Map<String, Object> valuesMap) {
                Pair<String, Object[]> parseResult = parseSelectSql(valuesMap, true, true);
                Long count = jdbcTemplate.query(parseResult.getKey(), rs -> {
                    return rs.getLong(1);
                }, parseResult.getValue());
                return null == count ? Boolean.FALSE : count > 0;
            }

            public Long count(Map<String, Object> valuesMap) {
                Pair<String, Object[]> parseResult = parseSelectSql(valuesMap, true, false);
                Long count = jdbcTemplate.query(parseResult.getKey(), rs -> {
                    return rs.getLong(1);
                }, parseResult.getValue());
                return null == count ? 0 : count;
            }

            public Page selectPage(Map<String, Object> valuesMap) {
                Page page = new Page();
                if (!valuesMap.containsKey(DaoConstants.PAGING_SIZE)
                        || !valuesMap.containsKey(DaoConstants.PAGING_CURRENT)) {
                    return new Page();
                }

                Long count = count(valuesMap);
                page.setCurrent((Long) valuesMap.get(DaoConstants.PAGING_CURRENT));
                page.setSize((Long) valuesMap.get(DaoConstants.PAGING_SIZE));
                if (count <= 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList<>(0));
                    return page;
                }

                page.setTotal(count);
                Pair<String, Object[]> parseResult = parseSelectPageSql(valuesMap);
                List<Map<String, Object>> result = jdbcTemplate.query(parseResult.getKey(), (rs, rowNum) -> {
                    Map<String, Object> resultMap = new HashMap<>();
                    for (String column : tableDefinition.getColumns().keySet()) {
                        resultMap.put(column, rs.getObject(column));
                    }
                    return resultMap;
                }, parseResult.getValue());

                page.setRecords(result);
                return page;
            }

            private Pair<String, Object[]> parseSelectPageSql(Map<String, Object> valuesMap){
                Pair<String, Object[]>  parseResult = parseSelectSql(valuesMap, false, false);
                String sql = parseResult.getKey();
                Object[] params = parseResult.getValue();
                sql += " LIMIT ?, ?";
                int index = 0;
                if (ArrayUtil.isEmpty(params)) {
                    params = new Object[2];
                }else{
                    index = params.length - 1;
                    params = ArrayUtil.resize(params, params.length + 2);
                }

                Integer current = (Integer) valuesMap.get(DaoConstants.PAGING_CURRENT);
                Integer pageSize = (Integer) valuesMap.get(DaoConstants.PAGING_SIZE);
                params[++index] = (current - 1) * pageSize;
                params[++index] = pageSize;
                return new Pair<>(sql, params);
            }

            private Pair<String, Object[]> parseSelectSql(Map<String, Object> valuesMap, boolean isCount, boolean useLimitOne) {
                StringBuilder sqlBuilder = new StringBuilder();
                Set<String> columns = tableDefinition.getColumns().keySet();
                sqlBuilder.append("SELECT ")
                        .append(isCount ? "COUNT(*)" : StrUtil.join(",", columns))
                        .append(" FROM ")
                        .append(tableDefinition.getName());
                Object[] params = null;
                if (MapUtil.isNotEmpty(valuesMap)) {
                    int index = 0;
                    params = new Object[valuesMap.size()];
                    sqlBuilder.append(" WHERE ");
                    for (Map.Entry<String, Object> entry : valuesMap.entrySet()) {
                        sqlBuilder.append((index == 0) ? StrUtil.EMPTY : " AND ")
                                .append(entry.getKey())
                                .append(" = ?");
                        params[index] = entry.getValue();
                        index++;
                    }
                }

                if (useLimitOne) {
                    sqlBuilder.append(" LIMIT 1");
                }

                return new Pair<>(sqlBuilder.toString(), params);
            }
        };
    }

}
