/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
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
