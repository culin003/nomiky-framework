/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import cn.hutool.core.map.MapUtil;
import lombok.Data;
import org.nomiky.nomikyframework.executor.FieldTypeConverter;
import org.nomiky.nomikyframework.executor.FieldTypeConverterManager;
import org.nomiky.nomikyframework.executor.FieldValueAutoGenaratorHelper;
import org.nomiky.nomikyframework.executor.FieldValueAutoGenerator;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author nomiky
 * @since 2024年01月23日 18时09分
 */
@Data
public class FrameworkConfig implements InitializingBean {

    boolean useLogicDelete = true;

    boolean printSql = true;

    public Map<String, FieldValueAutoGenerator> createValueAutoGenerator() {
        return null;
    }

    public Map<Type, FieldTypeConverter> createFieldTypeConverters() {
        return null;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, FieldValueAutoGenerator> valueAutoGeneratorMap = createValueAutoGenerator();
        if (MapUtil.isEmpty(valueAutoGeneratorMap)) {
            return;
        }

        valueAutoGeneratorMap.forEach(FieldValueAutoGenaratorHelper::register);

        Map<Type, FieldTypeConverter> fieldTypeConverterMap = createFieldTypeConverters();
        if (MapUtil.isEmpty(fieldTypeConverterMap)) {
            return;
        }

        fieldTypeConverterMap.forEach(FieldTypeConverterManager::register);
    }
}
