/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 拦截器上下文，串通前置和后置拦截器
 *
 * @author nomiky
 * @since 2023年12月25日 17时52分
 */
@Data
public class InterceptorContext {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final Map<String, Object> contextMap = new HashMap<>();

    private Object value;

    public InterceptorContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
}
