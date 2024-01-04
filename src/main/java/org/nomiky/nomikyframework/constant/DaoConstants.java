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
     * 从request流中获取到的字符串参数
     */
    String BODY_STRING = "bodyString";

    /**
     * 上个Executor的结果
     */
    String PARENT_RESULT = "parent";

    /**
     * 从request头中获取到的参数
     */
    String REQUEST_HEADER = "header";

    /**
     * 从request中获取到的参数
     */
    String REQUEST_PARAMETER = "param";

    /**
     * 从request流中获取到的字符串转换成json对象后的参数
     */
    String BODY_JSON = "bodyJson";

    String DAO_EXECUTOR_BEAN_NAME_SUFFIX = "_bean";

    /**
     * 在拼装SQL语句的引擎环境中定义的SQL语句变量名称
     */
    String SQL_ASSEMBLY_RESULT = "sqlResult";

    /**
     * 在拼装SQL语句的引擎环境中定义的SQL参数变量名称
     */
    String SQL_ASSEMBLY_PARAMETER = "sqlParams";
}
