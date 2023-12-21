/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.exception;

/**
 * 数据库操作异常
 *
 * @author nomiky
 * @since 2023年12月21日 18时49分
 */
public class ExecutorException extends RuntimeException {

    public ExecutorException() {
        super();
    }

    public ExecutorException(String msg) {
        super(msg);
    }

    public ExecutorException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
