/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.nomiky.nomikyframework.entity.TableDefinition;
import org.nomiky.nomikyframework.entity.XmlTable;
import org.nomiky.nomikyframework.enums.ExecutorEnum;
import org.nomiky.nomikyframework.util.Checker;
import org.nomiky.nomikyframework.util.DataTypeConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.lang.reflect.Type;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将数据库表解析到内存中缓存起来
 *
 * @author nomiky
 * @since 2023年12月22日 09时51分
 */
@Slf4j
public class TableDefinitionBeanProcessor {

    private final JdbcTemplate jdbcTemplate;

    public TableDefinitionBeanProcessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String QUERY_COLUMN_TYPE_SQL = "SELECT * FROM {} LIMIT 1";

    private final Map<String, TableDefinition> tableDefinitionMap = new HashMap<>();

    public void explainTables(List<XmlTable> xmlTables) {
        if (CollUtil.isEmpty(xmlTables)) {
            return;
        }

        Checker.checkEmpty(ExecutorEnum.JDBC_TEMPLATE_IS_EMPTY, jdbcTemplate);
        for (XmlTable xmlTable : xmlTables) {
            TableDefinition tableDefinition = explainTable(jdbcTemplate, xmlTable);
            tableDefinition.setFileName(xmlTable.getFileName());
            tableDefinitionMap.put(StrUtil.isEmpty(xmlTable.getSchema())
                    ? xmlTable.getName()
                    : (xmlTable.getSchema() + '.' + xmlTable.getName()), tableDefinition);
        }
    }

    public TableDefinition getTableDefinition(String tableName) {
        return tableDefinitionMap.getOrDefault(tableName, null);
    }

    public Map<String, TableDefinition> getTableDefinitions() {
        return tableDefinitionMap;
    }

    private TableDefinition explainTable(JdbcTemplate jdbcTemplate, XmlTable xmlTable) {
        TableDefinition tableDefinition = new TableDefinition();
        tableDefinition.setName(xmlTable.getName());
        tableDefinition.setPrimaryKeyGenerator(xmlTable.getPrimaryKeyGenerator());
        String primaryKey = getPrimaryKey(jdbcTemplate, xmlTable);
        tableDefinition.setPrimaryKey(primaryKey);
        LinkedHashMap<String, Type> columnMap = getColumnAndTypes(jdbcTemplate, xmlTable);
        tableDefinition.setColumns(columnMap);
        log.info("Explain table: {}, primary key: {}, columns: {}", xmlTable.getName(), primaryKey, StrUtil.join(StrUtil.COMMA, columnMap.keySet()));
        return tableDefinition;
    }

    private String getPrimaryKey(JdbcTemplate jdbcTemplate, XmlTable xmlTable) {
        return jdbcTemplate.execute((ConnectionCallback<String>) con -> {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet resultSet = metaData.getPrimaryKeys(con.getCatalog(), xmlTable.getSchema(), xmlTable.getName());
            String primaryKey = StrUtil.EMPTY;
            if (resultSet.next()) {
                primaryKey = resultSet.getString("COLUMN_NAME");
            } else {
                log.warn("The database table {} does not have a primary key!", xmlTable.getName());
            }

            return primaryKey;
        });
    }

    private LinkedHashMap<String, Type> getColumnAndTypes(JdbcTemplate jdbcTemplate, XmlTable xmlTable) {
        LinkedHashMap<String, Type> columnMap = new LinkedHashMap<>();
        String sql = StrUtil.format(QUERY_COLUMN_TYPE_SQL, xmlTable.getName());
        jdbcTemplate.query(sql, rs -> {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                columnMap.put(metaData.getColumnName(i),
                        DataTypeConverter.getInstance().getJavaType(metaData.getColumnType(i)));
            }
            return null;
        });

        return columnMap;
    }
}
