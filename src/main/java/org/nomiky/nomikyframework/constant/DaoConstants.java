package org.nomiky.nomikyframework.constant;

public interface DaoConstants {

    /**
     * 分页参数：当前页
     */
    String PAGING_CURRENT = "pageCurrent";

    /**
     * 分页参数：每页数据量
     */
    String PAGING_SIZE = "pageSize";

    /**
     * 分页参数：查询到的数据总数
     */
    String PAGING_TOTAL = "pageTotal";

    /**
     * 分页参数：数据
     */
    String PAGING_RECORDS = "pageRecords";

    /**
     * 主键标识
     */
    String BODY_STRING = "bodyString";

    /**
     * 上个Executor的结果
     */
    String PARENT_RESULT = "parentResult";

    String DAO_EXECUTOR_BEAN_NAME_SUFFIX = "_bean";
}
