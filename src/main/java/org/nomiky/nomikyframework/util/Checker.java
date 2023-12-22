/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.nomiky.nomikyframework.enums.ExecutorEnum;
import org.nomiky.nomikyframework.exception.ExecutorException;

/**
 * 检查执行
 *
 * @author nomiky
 * @since 2023年12月21日 18时54分
 */
public class Checker {

    public static void checkEmpty(ExecutorEnum executorEnum, Object... params) {
        if (null == params) {
            throw new ExecutorException(executorEnum.getMessage());
        }

        for (Object param : params) {
            if (ObjectUtil.isEmpty(param)) {
                throw new ExecutorException(executorEnum.getMessage());
            }
        }
    }

    public static void checkAllEmpty(ExecutorEnum executorEnum, Object... params) {
        if (null == params) {
            throw new ExecutorException(executorEnum.getMessage());
        }

        for (Object param : params) {
            if (ObjectUtil.isNotEmpty(param)) {
                throw new ExecutorException(executorEnum.getMessage());
            }
        }
    }

    public static void checkBoolean(boolean express, String message) {
        if (!express) {
            throw new ExecutorException(message);
        }
    }
}
