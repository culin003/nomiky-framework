/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import java.util.Map;

/**
 * @author nomiky
 * @since 2024年01月23日 17时48分
 */
public interface FieldValueAutoGenerator {

    void generate(Map<String, Object> valueMap);

}
