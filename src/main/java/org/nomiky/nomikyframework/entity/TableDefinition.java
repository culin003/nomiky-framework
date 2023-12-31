/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 表
 *
 * @author nomiky
 * @since 2023年12月21日 18时41分
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TableDefinition extends XmlEntity {

    public static final String UUID_GENERATOR = "UUID";
    public static final String SNOWFLAKE_GENERATOR = "SNOWFLAKE";
    public static final String NANOID_GENERATOR = "NANOID";


    /**
     * 表名称
     */
    private String name;

    /**
     * <pre>
     * 表主键字段，默认只会有一个主键！会根据这个主键进行修改和删除。
     * 所以，一般要求数据库表中只存在一个唯一主键！
     * 如果需要联合主键，建议除了唯一主键以外，使用唯一索引来代替联合主键
     * </pre>
     */
    private String primaryKey;

    /**
     * ID产生器，支持：UUID、SNOWFLAKE、NANOID
     */
    private String primaryKeyGenerator = SNOWFLAKE_GENERATOR;

    /**
     * 表字段及其数据类型
     */
    private LinkedHashMap<String, Type> columns;

    /**
     * 根据ID生成策略生成ID
     *
     * @return ID
     */
    public Object generateId() {
        return switch (primaryKeyGenerator) {
            case UUID_GENERATOR -> IdUtil.fastSimpleUUID();
            case NANOID_GENERATOR -> IdUtil.nanoId();
            default -> IdUtil.getSnowflakeNextId();
        };
    }

    /**
     * 转换参数值为符合字段类型的值
     *
     * @param paramMap 参数名，驼峰格式，自动转换为下划线格式的字段名
     * @return 符合字段类型的实际值
     */
    public Map<String, Object> toDaoValueMap(Map<String, Object> paramMap) {
        if (MapUtil.isEmpty(paramMap)) {
            return paramMap;
        }

        Map<String, Object> valuesMap = new HashMap<>(paramMap.size());
        paramMap.forEach((k, v) -> {
            String columnName = StrUtil.toUnderlineCase(k);
            if (getColumns().containsKey(columnName)) {
                Type dataType = getColumns().get(columnName);
                valuesMap.put(columnName, toDataValue(v, dataType));
            }
        });

        return valuesMap;
    }

    private Object toDataValue(Object value, Type dataType) {
        // 从参数中解析出来的值默认只有两种类型：字符串、数字
        return Convert.convertWithCheck(dataType, value, null, false);
    }
}
