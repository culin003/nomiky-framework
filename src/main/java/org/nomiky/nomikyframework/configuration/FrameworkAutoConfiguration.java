/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.configuration;

import org.nomiky.nomikyframework.executor.DaoExecutorManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 框架配置
 *
 * @author nomiky
 * @since 2023年12月21日 11时21分
 */
@Configuration
public class FrameworkAutoConfiguration {

    @Bean
    public static DaoExecutorManager daoExecutorManager(){
        return new DaoExecutorManager();
    }

}
