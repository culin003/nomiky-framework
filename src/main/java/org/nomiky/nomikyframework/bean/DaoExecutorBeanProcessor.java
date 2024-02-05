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
import cn.hutool.json.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.nomiky.nomikyframework.constant.DaoConstants;
import org.nomiky.nomikyframework.entity.FrameworkConfig;
import org.nomiky.nomikyframework.entity.Page;
import org.nomiky.nomikyframework.entity.TableDefinition;
import org.nomiky.nomikyframework.exception.ExecutorException;
import org.nomiky.nomikyframework.executor.DaoExecutor;
import org.nomiky.nomikyframework.executor.FieldValueAutoGenaratorHelper;
import org.nomiky.nomikyframework.util.Checker;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.nomiky.nomikyframework.enums.ExecutorEnum.TABLE_EXPLAIN_ERROR;
import static org.nomiky.nomikyframework.enums.ExecutorEnum.TABLE_NAME_EMPTY;

/**
 * 生成表的Executor
 *
 * @author nomiky
 * @since 2023年12月21日 18时24分
 */
@Slf4j
public class DaoExecutorBeanProcessor {

    private DaoExecutorBeanProcessor() {
    }

    private final static class DaoExecutorBeanProcessorHelper {
        private static final DaoExecutorBeanProcessor INSTANCE = new DaoExecutorBeanProcessor();
    }

    public static DaoExecutorBeanProcessor getInstance() {
        return DaoExecutorBeanProcessorHelper.INSTANCE;
    }

    /**
     * 创建指定表的Executor
     *
     * @param tableDefinition 表
     * @return 指定表的Executor
     */
    public DaoExecutor createSpecifyExecutor(final TableDefinition tableDefinition, final JdbcTemplate jdbcTemplate, FrameworkConfig frameworkConfig) {
        return new DaoExecutor() {
            public String getTableName() {
                Checker.checkEmpty(TABLE_NAME_EMPTY, tableDefinition.getName());
                return tableDefinition.getName();
            }

            public Integer insert(Map<String, Object> valuesMap) {
                String primaryKey = tableDefinition.getPrimaryKey();
                Checker.checkEmpty(TABLE_EXPLAIN_ERROR, primaryKey);
                FieldValueAutoGenaratorHelper.autoGenerate(FieldValueAutoGenaratorHelper.INSERT, valuesMap);
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                StringBuilder sqlBuilder = new StringBuilder();
                final LinkedHashSet<String> columnSet = new LinkedHashSet<>(finalMap.keySet());
                columnSet.remove(primaryKey);
                List<String> columnList = columnSet.stream().map(s -> '`' + s + '`').collect(Collectors.toList());
                sqlBuilder.append("INSERT INTO ")
                        .append(getTableName())
                        .append('(')
                        .append('`')
                        .append(primaryKey)
                        .append('`')
                        .append(',')
                        .append(StrUtil.join(StrUtil.COMMA, columnList))
                        .append(')')
                        .append(" VALUES (");
                for (int i = 0; i <= columnSet.size(); i++) {
                    sqlBuilder.append((i == columnSet.size()) ? "?" : "?, ");
                }

                sqlBuilder.append(')');
                log.info(sqlBuilder.toString());
                return jdbcTemplate.update(sqlBuilder.toString(), ps -> {
                    ps.setObject(1, tableDefinition.generateId());
                    int index = 2;
                    for (String tableColumn : columnSet) {
                        if (finalMap.containsKey(tableColumn)) {
                            ps.setObject(index++, finalMap.get(tableColumn));
                        }
                    }
                });
            }

            public Integer deleteById(Map<String, Object> valuesMap) {
                String primaryKey = tableDefinition.getPrimaryKey();
                Checker.checkEmpty(TABLE_EXPLAIN_ERROR, primaryKey);
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("DELETE FROM ")
                        .append(tableDefinition.getName())
                        .append(" WHERE `")
                        .append(primaryKey)
                        .append("` = ?");
                FieldValueAutoGenaratorHelper.autoGenerate(FieldValueAutoGenaratorHelper.DELETE, valuesMap);
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                log.info(sqlBuilder.toString());
                return jdbcTemplate.update(sqlBuilder.toString(), finalMap.get(tableDefinition.getPrimaryKey()));
            }

            public Integer updateById(Map<String, Object> valuesMap) {
                String primaryKey = tableDefinition.getPrimaryKey();
                Checker.checkEmpty(TABLE_EXPLAIN_ERROR, primaryKey);
                if (!valuesMap.containsKey(primaryKey)) {
                    throw new ExecutorException("Primary key value is unset！");
                }

                FieldValueAutoGenaratorHelper.autoGenerate(FieldValueAutoGenaratorHelper.UPDATE, valuesMap);
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("UPDATE ")
                        .append(tableDefinition.getName())
                        .append(" SET");
                LinkedHashMap<String, Object> finalValueMap = new LinkedHashMap<>(finalMap);
                finalValueMap.forEach((k, v) -> {
                    if (!k.equalsIgnoreCase(primaryKey)) {
                        sqlBuilder.append(" `").append(k).append("` = ?,");
                    }
                });

                sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                sqlBuilder.append(" WHERE `").append(primaryKey).append("` = ?");

                log.info(sqlBuilder.toString());
                return jdbcTemplate.update(sqlBuilder.toString(), ps -> {
                    int index = 1;
                    for (Map.Entry<String, Object> entry : finalValueMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (!key.equalsIgnoreCase(primaryKey)) {
                            ps.setObject(index++, value);
                        }
                    }

                    ps.setObject(index, finalMap.get(primaryKey));
                });
            }

            public List<Map<String, Object>> select(Map<String, Object> valuesMap) {
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                Pair<String, Object[]> parseResult = parseSelectSql(finalMap, false, false);
                log.info(parseResult.getKey());
                return queryForMap(parseResult);
            }

            public Map<String, Object> selectOne(Map<String, Object> valuesMap) {
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                Pair<String, Object[]> parseResult = parseSelectSql(finalMap, false, true);
                log.info(parseResult.getKey());
                List<Map<String, Object>> result = queryForMap(parseResult);
                return CollUtil.isEmpty(result) ? new HashMap<>(0) : result.get(0);
            }

            public Boolean exist(Map<String, Object> valuesMap) {
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                Pair<String, Object[]> parseResult = parseSelectSql(finalMap, true, true);
                log.info(parseResult.getKey());
                Long count = jdbcTemplate.query(parseResult.getKey(), rs -> {
                    if (rs.next()) {
                        return rs.getLong(1);
                    } else {
                        return 0L;
                    }
                }, parseResult.getValue());
                return null == count ? Boolean.FALSE : count > 0;
            }

            public Long count(Map<String, Object> valuesMap) {
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                Pair<String, Object[]> parseResult = parseSelectSql(finalMap, true, false);
                log.info(parseResult.getKey());
                Long count = jdbcTemplate.query(parseResult.getKey(), rs -> {
                    if (rs.next()) {
                        return rs.getLong(1);
                    } else {
                        return 0L;
                    }
                }, parseResult.getValue());
                return null == count ? 0 : count;
            }

            public Page selectPage(Map<String, Object> valuesMap) {
                Page page = new Page();
                if (!valuesMap.containsKey(DaoConstants.PAGING_SIZE)
                        || !valuesMap.containsKey(DaoConstants.PAGING_CURRENT)) {
                    return new Page();
                }

                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                Long count = count(finalMap);
                page.setCurrent(Long.parseLong(valuesMap.get(DaoConstants.PAGING_CURRENT).toString()));
                page.setSize(Long.parseLong(valuesMap.get(DaoConstants.PAGING_SIZE).toString()));
                if (count <= 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList<>(0));
                    return page;
                }

                page.setTotal(count);
                Pair<String, Object[]> parseResult = parseSelectPageSql(finalMap, page);
                log.info(parseResult.getKey());
                page.setRecords(queryForMap(parseResult));
                return page;
            }

            @Override
            public void batchInsert(JSONArray params) {
                String primaryKey = tableDefinition.getPrimaryKey();
                Checker.checkEmpty(TABLE_EXPLAIN_ERROR, primaryKey);
                StringBuilder sqlBuilder = new StringBuilder();
                Map<String, Object> valuesMap = (Map<String, Object>) params.get(0);
                FieldValueAutoGenaratorHelper.autoGenerate(FieldValueAutoGenaratorHelper.INSERT, valuesMap);
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                final LinkedHashSet<String> columnSet = new LinkedHashSet<>(finalMap.keySet());
                List<String> columnList = columnSet.stream().map(s -> '`' + s + '`').collect(Collectors.toList());
                sqlBuilder.append("INSERT INTO ")
                        .append(getTableName())
                        .append('(')
                        .append('`')
                        .append(primaryKey)
                        .append('`')
                        .append(',')
                        .append(StrUtil.join(StrUtil.COMMA, columnList))
                        .append(')')
                        .append(" VALUES (");
                for (int i = 0; i <= columnSet.size(); i++) {
                    sqlBuilder.append((i == columnSet.size()) ? "?" : "?, ");
                }

                sqlBuilder.append(')');
                log.info(sqlBuilder.toString());
                jdbcTemplate.batchUpdate(sqlBuilder.toString(), new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Map<String, Object> tempFinalMap;
                        if (i == 0) {
                            tempFinalMap = finalMap;
                        }else {
                            Map<String, Object> tempValuesMap = (Map<String, Object>) params.get(i);
                            FieldValueAutoGenaratorHelper.autoGenerate(FieldValueAutoGenaratorHelper.INSERT, tempValuesMap);
                            tempFinalMap = tableDefinition.toDaoValueMap(tempValuesMap);
                        }
                        ps.setObject(1, tableDefinition.generateId());
                        int index = 2;
                        for (String tableColumn : columnSet) {
                            if (tempFinalMap.containsKey(tableColumn)) {
                                ps.setObject(index++, tempFinalMap.get(tableColumn));
                            }
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return params.size();
                    }
                });
            }

            @Override
            public void batchUpdate(JSONArray params) {
                String primaryKey = tableDefinition.getPrimaryKey();
                Checker.checkEmpty(TABLE_EXPLAIN_ERROR, primaryKey);
                Map<String, Object> valuesMap = (Map<String, Object>) params.get(0);
                if (!valuesMap.containsKey(primaryKey)) {
                    throw new ExecutorException("Primary key value is unset！");
                }

                FieldValueAutoGenaratorHelper.autoGenerate(FieldValueAutoGenaratorHelper.UPDATE, valuesMap);
                Map<String, Object> finalMap = tableDefinition.toDaoValueMap(valuesMap);
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("UPDATE ")
                        .append(tableDefinition.getName())
                        .append(" SET");
                LinkedHashSet<String> finalKeySet = new LinkedHashSet<>(finalMap.keySet());
                finalKeySet.forEach(k-> {
                    if (!k.equalsIgnoreCase(primaryKey)) {
                        sqlBuilder.append(" `").append(k).append("` = ?,");
                    }
                });

                sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
                sqlBuilder.append(" WHERE `").append(primaryKey).append("` = ?");

                jdbcTemplate.update(sqlBuilder.toString(), new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int index = 1;
                        Map<String, Object> tempFinalMap;
                        if (i == 0) {
                            tempFinalMap = finalMap;
                        }else{
                            Map<String, Object> tempValuesMap = (Map<String, Object>) params.get(i);
                            FieldValueAutoGenaratorHelper.autoGenerate(FieldValueAutoGenaratorHelper.UPDATE, tempValuesMap);
                            tempFinalMap = tableDefinition.toDaoValueMap(tempValuesMap);
                        }
                        for (String key : finalKeySet) {
                            if (!key.equalsIgnoreCase(primaryKey)) {
                                ps.setObject(index++, tempFinalMap.get(key));
                            }
                        }

                        ps.setObject(index, tempFinalMap.get(primaryKey));
                    }

                    @Override
                    public int getBatchSize() {
                        return params.size();
                    }
                });
            }

            private Pair<String, Object[]> parseSelectPageSql(Map<String, Object> valuesMap, Page page) {
                Pair<String, Object[]> parseResult = parseSelectSql(valuesMap, false, false);
                String sql = parseResult.getKey();
                Object[] params = parseResult.getValue();
                sql += " LIMIT ?, ?";
                int index = 0;
                if (ArrayUtil.isEmpty(params)) {
                    params = new Object[2];
                } else {
                    index = params.length;
                    params = ArrayUtil.resize(params, params.length + 2);
                }

                params[index++] = (page.getCurrent() - 1) * page.getSize();
                params[index] = page.getSize();
                return new Pair<>(sql, params);
            }

            private Pair<String, Object[]> parseSelectSql(Map<String, Object> valuesMap, boolean isCount, boolean useLimitOne) {
                StringBuilder sqlBuilder = new StringBuilder();
                Set<String> columns = tableDefinition.getColumns().keySet();
                List<String> columnList = columns.stream().map(s -> '`' + s + '`').collect(Collectors.toList());
                sqlBuilder.append("SELECT ")
                        .append(isCount ? "COUNT(*)" : StrUtil.join(StrUtil.COMMA, columnList))
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

            private List<Map<String, Object>> queryForMap(Pair<String, Object[]> parseResult) {
                return jdbcTemplate.query(parseResult.getKey(), (rs, rowNum) -> {
                    Map<String, Object> resultMap = new HashMap<>();
                    for (String column : tableDefinition.getColumns().keySet()) {
                        resultMap.put(StrUtil.toCamelCase(column), rs.getObject(column));
                    }
                    return resultMap;
                }, parseResult.getValue());
            }
        };
    }


}
