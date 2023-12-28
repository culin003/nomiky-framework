/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import org.nomiky.nomikyframework.constant.DaoConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author nomiky
 * @since 2023年12月27日 16时18分
 */
@Component
public class DaoExecuterManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public DaoExecutor getExecutor(String tableName) {
        return (DaoExecutor) applicationContext.getBean(tableName + DaoConstants.DAO_EXECUTOR_BEAN_NAME_SUFFIX);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
