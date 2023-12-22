/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.enums;

/**
 * @author nomiky
 * @since 2023年12月21日 18时57分
 */
public enum ExecutorEnum {

    TABLE_NAME_EMPTY(10000, "表名为空"),
    TABLE_EXPLAIN_ERROR(10001, "表解析错误"),
    TABLE_NAME_NOT_SPECIFY(10002, "在tableDefine.xml中发现表名未定义"),
    JDBC_TEMPLATE_IS_EMPTY(20000, "jdbcTemplate为空"),
    ;

    private Integer code;

    private String message;

    ExecutorEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
