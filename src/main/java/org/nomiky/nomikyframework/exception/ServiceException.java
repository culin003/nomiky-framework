/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author nomiky
 * @since 2024年01月24日 13时42分
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceException extends RuntimeException {

    private int code;

    public ServiceException() {
        super();
        this.code = 500;
    }

    public ServiceException(String msg) {
        super(msg);
        this.code = 500;
    }

    public ServiceException(String msg, Throwable throwable) {
        super(msg, throwable);
        this.code = 500;
    }

    public ServiceException(int code) {
        super();
        this.code = code;
    }

    public ServiceException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public ServiceException(int code, String msg, Throwable throwable) {
        super(msg, throwable);
        this.code = code;
    }
}
