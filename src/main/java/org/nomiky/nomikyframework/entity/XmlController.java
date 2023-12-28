/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import org.nomiky.nomikyframework.interceptor.NomikyInterceptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 在XML中定义的controller
 *
 * @author nomiky
 * @since 2023年12月22日 16时58分
 */
@Data
public class XmlController {

    /**
     * 映射路径
     */
    private String path;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 数据类型
     */
    private String consume;

    /**
     * 是否使用事务执行Executors
     */
    private Boolean useTransaction;

    /**
     * 作用在controller的拦截器
     */
    private Set<String> interceptors = new HashSet<>();

    /**
     * controller需要执行的业务
     */
    private List<XmlExecutor> executors;

    private List<NomikyInterceptor> beforeInterceptors;

    private List<NomikyInterceptor> afterInterceptors;

    public void setInterceptors(Set<String> interceptors) {
        this.interceptors = interceptors;
        splitInterceptors();
    }

    /**
     * 拆分前后置拦截器
     */
    public void splitInterceptors() {
        for (String interceptor : getInterceptors()) {
            NomikyInterceptor interceptorInstance = ReflectUtil.newInstance(interceptor);
            if (interceptorInstance.isBefore()) {
                this.beforeInterceptors.add(interceptorInstance);
            } else {
                this.afterInterceptors.add(interceptorInstance);
            }
        }
    }

}
