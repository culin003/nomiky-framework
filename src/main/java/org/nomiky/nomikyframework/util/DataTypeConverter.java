/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库类型转JAVA类型
 *
 * @author nomiky
 * @since 2023年12月22日 11时09分
 */
public class DataTypeConverter {

    private final Map<Integer, Type> sqlAndJavaTypeMap;

    private static final class DataTypeConverterHelper {
        private static final DataTypeConverter DATA_TYPE_CONVERTER = new DataTypeConverter();
    }

    public static DataTypeConverter getInstance() {
        return DataTypeConverterHelper.DATA_TYPE_CONVERTER;
    }

    private DataTypeConverter() {
        sqlAndJavaTypeMap = new HashMap<>();
        sqlAndJavaTypeMap.put(Types.BIGINT, long.class);
        sqlAndJavaTypeMap.put(Types.BIT, boolean.class);
        sqlAndJavaTypeMap.put(Types.BINARY, byte[].class);
        sqlAndJavaTypeMap.put(Types.DATE, Date.class);
        sqlAndJavaTypeMap.put(Types.BLOB, Blob.class);
        sqlAndJavaTypeMap.put(Types.BOOLEAN, boolean.class);
        sqlAndJavaTypeMap.put(Types.CHAR, String.class);
        sqlAndJavaTypeMap.put(Types.CLOB, Clob.class);
        sqlAndJavaTypeMap.put(Types.DATALINK, URL.class);
        sqlAndJavaTypeMap.put(Types.DECIMAL, BigDecimal.class);
        sqlAndJavaTypeMap.put(Types.DOUBLE, double.class);
        sqlAndJavaTypeMap.put(Types.FLOAT, float.class);
        sqlAndJavaTypeMap.put(Types.REAL, float.class);
        sqlAndJavaTypeMap.put(Types.INTEGER, int.class);
        sqlAndJavaTypeMap.put(Types.LONGVARBINARY, byte[].class);
        sqlAndJavaTypeMap.put(Types.VARBINARY, byte[].class);
        sqlAndJavaTypeMap.put(Types.NCHAR, String.class);
        sqlAndJavaTypeMap.put(Types.NCLOB, NClob.class);
        sqlAndJavaTypeMap.put(Types.NUMERIC, BigDecimal.class);
        sqlAndJavaTypeMap.put(Types.VARCHAR, String.class);
        sqlAndJavaTypeMap.put(Types.NVARCHAR, String.class);
        sqlAndJavaTypeMap.put(Types.LONGVARCHAR, String.class);
        sqlAndJavaTypeMap.put(Types.SMALLINT, int.class);
        sqlAndJavaTypeMap.put(Types.TINYINT, int.class);
        sqlAndJavaTypeMap.put(Types.TIMESTAMP, Date.class);
        sqlAndJavaTypeMap.put(Types.TIMESTAMP_WITH_TIMEZONE, Date.class);
    }

    public Type getJavaType(int sqlType) {
        return sqlAndJavaTypeMap.getOrDefault(sqlType, Object.class);
    }
}
