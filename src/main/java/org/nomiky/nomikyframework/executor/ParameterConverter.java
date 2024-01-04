/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 参数转换器
 *
 * @author nomiky
 * @since 2023年12月25日 16时58分
 */
public interface ParameterConverter {

    /**
     * 参数转换
     *
     * @param paramsRef    参数定义
     * @param request      请求对象
     * @param parentParams 上一个执行结果
     * @return 转换后的参数
     */
    Map<String, Object> convert(String paramsRef, HttpServletRequest request, Object parentParams) ;

}
