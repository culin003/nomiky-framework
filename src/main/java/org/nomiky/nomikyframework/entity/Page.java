/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 分页对象
 *
 * @author nomiky
 * @since 2023年12月21日 18时16分
 */
@Data
public class Page implements Serializable {

    /**
     * 数据
     */
    protected List<Map<String, Object>> records;

    /**
     * 总数
     */
    protected long total;

    /**
     * 一页需要查询的数据量
     */
    protected long size;

    /**
     * 当前页
     */
    protected long current;

}
