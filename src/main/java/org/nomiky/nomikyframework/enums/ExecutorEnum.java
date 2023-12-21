/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.enums;

/**
 * @author nomiky
 * @since 2023年12月21日 18时57分
 */
public enum ExecutorEnum {

    TABLE_NAME_EMPTY(10000, "表名为空"),
    TABLE_EXPLAIN_ERROR(10001, "表解析错误"),
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