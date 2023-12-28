/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import org.nomiky.nomikyframework.entity.Page;

import java.util.List;
import java.util.Map;

/**
 * 数据库执行器，默认执行操作包含：insert,deleteById,updateById,select,selectOne,exist,count,selectPage
 *
 * @author nomiky
 * @since 2023年12月21日 11时22分
 */
public interface DaoExecutor {

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    String getTableName();

    /**
     * 插入数据
     *
     * @param valuesMap 包含字段属性名称和值的映射集合，默认将驼峰属性名称转换成数据库表字段使用的下划线名称
     * @return 插入的数据条数
     */
    int insert(Map<String, Object> valuesMap);

    /**
     * 根据表主键字段删除数据
     *
     * @param valuesMap 主键值
     * @return 删除的数据条数
     */
    int deleteById(Map<String, Object> valuesMap);

    /**
     * 根据表主键字段更新数据
     *
     * @param valuesMap 包含字段属性名称和值的映射集合，默认将驼峰属性名称转换成数据库表字段使用的下划线名称
     * @return 更新的数据条数
     */
    int updateById(Map<String, Object> valuesMap);

    /**
     * 根据条件查询表数据
     *
     * @param valuesMap 查询条件：包含字段属性名称和值的映射集合，默认将驼峰属性名称转换成数据库表字段使用的下划线名称
     * @return 查询结果集，使用Map表示，自动将表字段名称的下划线改为驼峰作为key，value为字段值
     */
    List<Map<String, Object>> select(Map<String, Object> valuesMap);

    /**
     * 根据条件查询表数据，只返回一条记录。默认会在SQL语句后添加 limit 1 语句
     *
     * @param valuesMap 查询条件：包含字段属性名称和值的映射集合，默认将驼峰属性名称转换成数据库表字段使用的下划线名称
     * @return 查询结果集，使用Map表示，自动将表字段名称的下划线改为驼峰作为key，value为字段值
     */
    Map<String, Object> selectOne(Map<String, Object> valuesMap);

    /**
     * 查询表数据中是否存在指定条件的数据，默认会在SQL语句后添加 limit 1 语句
     *
     * @param valuesMap 查询条件：包含字段属性名称和值的映射集合，默认将驼峰属性名称转换成数据库表字段使用的下划线名称
     * @return 存在时返回 true，不存在时返回：false
     */
    Boolean exist(Map<String, Object> valuesMap);

    /**
     * 根据条件查询表数据数量
     *
     * @param valuesMap 查询条件：包含字段属性名称和值的映射集合，默认将驼峰属性名称转换成数据库表字段使用的下划线名称
     * @return 表数据数量
     */
    Long count(Map<String, Object> valuesMap);

    /**
     * 分页查询
     *
     * @param valuesMap 查询条件：包含字段属性名称和值的映射集合，默认将驼峰属性名称转换成数据库表字段使用的下划线名称
     * @return 一页表数据数
     */
    Page selectPage(Map<String, Object> valuesMap);
}
