/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.InvocationTargetRuntimeException;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.nomiky.nomikyframework.constant.DaoConstants;
import org.nomiky.nomikyframework.entity.R;
import org.nomiky.nomikyframework.entity.XmlController;
import org.nomiky.nomikyframework.entity.XmlExecutor;
import org.nomiky.nomikyframework.entity.XmlMapper;
import org.nomiky.nomikyframework.exception.ExecutorException;
import org.nomiky.nomikyframework.exception.ServiceException;
import org.nomiky.nomikyframework.executor.DaoExecutor;
import org.nomiky.nomikyframework.executor.FieldTypeConverterManager;
import org.nomiky.nomikyframework.executor.ParameterConverter;
import org.nomiky.nomikyframework.executor.RequestHandler;
import org.nomiky.nomikyframework.interceptor.InterceptorContext;
import org.nomiky.nomikyframework.interceptor.NomikyInterceptor;
import org.nomiky.nomikyframework.util.RequestUtil;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.sql.rowset.serial.SerialException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * 生成Controller Mapper
 *
 * @author nomiky
 * @since 2023年12月22日 16时39分
 */
@Data
@Slf4j
public class ControllerBeanProcessor {

    private Map<String, XmlMapper> mappers;

    private Set<String> interceptors;

    private List<XmlController> controllers;

    private Map<String, DaoExecutor> executorMap;

    private RequestMappingHandlerMapping handlerMapping;

    private ParameterConverter parameterConverter;

    private DataSourceTransactionManager transactionManager;

    private JdbcTemplate jdbcTemplate;

    public ControllerBeanProcessor() {
    }

    public void mapHandlerMapping() throws NoSuchMethodException {
        if (CollUtil.isEmpty(controllers)) {
            return;
        }

        for (XmlController controller : controllers) {
            registerMapping(controller);
            log.info("Register controller mapping handler, path: {}, method: {}",
                    controller.getPath(),
                    StrUtil.isEmpty(controller.getMethod()) ? HttpMethod.GET.name() : controller.getMethod());
        }
    }

    private void registerMapping(XmlController controller) throws NoSuchMethodException {
        RequestMappingInfo mappingInfo = RequestMappingInfo.paths(controller.getPath())
                .methods(StrUtil.isEmpty(controller.getMethod())
                        ? RequestMethod.GET
                        : RequestMethod.resolve(controller.getMethod().toUpperCase()))
                .consumes(StrUtil.isEmpty(controller.getConsume())
                        ? new String[0]
                        : new String[]{controller.getConsume()})
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .build();


        RequestHandler requestHandler = createHandler(controller);
        handlerMapping.registerMapping(
                mappingInfo,
                requestHandler,
                requestHandler.getClass().getMethod("handler", HttpServletRequest.class, HttpServletResponse.class)
        );
    }

    private RequestHandler createHandler(XmlController controller) {
        return (request, response) -> {
            List<XmlExecutor> executors = controller.getExecutors();
            if (CollUtil.isEmpty(executors)) {
                return R.fail("Can not find DaoExecutor for controller mapping: " + controller.getPath());
            }

            // 处理Executor
            try {
                // 置前拦截器
                InterceptorContext context = new InterceptorContext(request, response);
                List<NomikyInterceptor> beforeInterceptors = controller.getBeforeInterceptors();
                if (CollUtil.isNotEmpty(beforeInterceptors)) {
                    for (NomikyInterceptor beforeInterceptor : beforeInterceptors) {
                        beforeInterceptor.process(context);
                    }
                }
                Object value = StrUtil.EMPTY;
                boolean isUseTransaction = controller.getUseTransaction();
                if (isUseTransaction) {
                    TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
                    TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
                    try {
                        for (XmlExecutor executor : executors) {
                            value = doExecutor(executor, request, value);
                        }
                        transactionManager.commit(transactionStatus);
                    } catch (Exception e) {
                        transactionManager.rollback(transactionStatus);
                        log.error("Executor fail!", e);
                    }
                } else {
                    for (XmlExecutor executor : executors) {
                        value = doExecutor(executor, request, value);
                    }
                }
                // 后置拦截器
                List<NomikyInterceptor> afterInterceptors = controller.getAfterInterceptors();
                if (CollUtil.isNotEmpty(afterInterceptors)) {
                    context.setValue(value);
                    for (NomikyInterceptor afterInterceptor : afterInterceptors) {
                        afterInterceptor.process(context);
                    }
                }

                return R.data(value);
            } catch (ServiceException se) {
                log.error("Service exception!!", se);
                return R.fail(se.getCode(), se.getMessage());
            } catch (InvocationTargetRuntimeException ite) {
                log.error("Occur exception!!", ite);
                Throwable target = ite.getCause().getCause();
                if (target instanceof ServiceException se) {
                    return R.fail(se.getCode(), se.getMessage());
                }
                return R.fail();
            } catch (Exception e) {
                log.error("Run executor occur exception!!", e);
                return R.fail();
            }
        };
    }

    private Object doExecutor(XmlExecutor executor, HttpServletRequest request, Object parentParams) throws Exception {
        if (XmlExecutor.TYPE_SQL.equals(executor.getType())) {
            return doSqlDaoExecutor(executor, request, parentParams);
        } else if (XmlExecutor.TYPE_BEAN.equals(executor.getType())) {
            return doBeanDaoExecutor(executor, request, parentParams);
        } else {
            return doDefaultDaoExecutor(executor, request, parentParams);
        }
    }

    private Object doBeanDaoExecutor(XmlExecutor executor, HttpServletRequest request, Object parentParams) {
        String beanRef = executor.getRef();
        List<String> beanInfo = StrUtil.split(beanRef, StrUtil.DOT);
        if (CollUtil.isEmpty(beanInfo) || beanInfo.size() != 2) {
            throw new ExecutorException("文件：" + executor.getFileName() + "定义的ref：" + beanRef + "不正确，" +
                    "请使用beanName.beanMethod的方式定义bean及方法引用！！");
        }

        String beanName = beanInfo.get(0);
        String beanMethod = beanInfo.get(1);
        Object bean = SpringUtil.getBean(beanName);
        if (null == bean) {
            throw new ExecutorException("文件：" + executor.getFileName() + "定义的ref：" + beanRef + "不正确，" +
                    "未能找到名称为" + beanName + "的bean！！");
        }

        Map<String, Object> valueMap = parameterConverter.convert(executor.getParams(), request, parentParams);
        Class<?> daoClass = bean.getClass();
        Method method = ReflectUtil.getMethod(daoClass, beanMethod, Map.class);
        if (null == method) {
            throw new ExecutorException("文件：" + executor.getFileName() + "定义的ref：" + beanRef + "不正确，" +
                    "未能找到名称为" + beanMethod + "的bean方法！！");
        }

        return ReflectUtil.invoke(bean, method, valueMap);
    }

    private Object doSqlDaoExecutor(XmlExecutor executor, HttpServletRequest request, Object parentParams) throws Exception {
        String sqlDefinition = executor.getSqlDefinition();
        if (StrUtil.isEmpty(sqlDefinition)) {
            throw new ExecutorException("Empty sql definition in xml file " + executor.getFileName());
        }

        // 使用的SQL拼接引擎
        Pair<String, Object[]> realSqlDefinition = getSql(executor.getEngine(), sqlDefinition, request, parentParams);

        // 查询操作
        if (XmlExecutor.OPERATOR_SELECT.equalsIgnoreCase(executor.getOperator())) {
            if (StrUtil.isEmpty(realSqlDefinition.getKey())) {
                throw new ExecutorException(StrUtil.format("Invalid executor sql definition! path: {}, file: {}",
                        request.getRequestURI(), executor.getFileName()));
            }

            log.info(realSqlDefinition.getKey());
            return jdbcTemplate.query(realSqlDefinition.getKey(), (rs, rowNum) -> {
                Map<String, Object> resultMap = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    resultMap.put(StrUtil.toCamelCase(metaData.getColumnName(i)), rs.getObject(i));
                }

                return resultMap;
            }, realSqlDefinition.getValue());
        }
        else if (XmlExecutor.OPERATOR_BATCH.equalsIgnoreCase(executor.getOperator())) {
            Object[] batchSqls = realSqlDefinition.getValue();
            int count = 1;
            if (batchSqls.length > 1000) {
                count = batchSqls.length % 1000 == 0 ? batchSqls.length / 1000 : batchSqls.length / 1000 + 1;
            }

            String[] sqls = Arrays.stream(batchSqls).map(Object::toString).toArray(String[]::new);
            TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
            try {
                for (int i = 0; i < count; i++) {
                    jdbcTemplate.batchUpdate(ArrayUtil.sub(sqls, i * 1000, (i + 1) * 1000));
                }
                transactionManager.commit(transactionStatus);
                return true;
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                log.error("Executor fail!", e);
                return false;
            }
        }
        // 更新操作
        else {
            if (StrUtil.isEmpty(realSqlDefinition.getKey())) {
                throw new ExecutorException(StrUtil.format("Invalid executor sql definition! path: {}, file: {}",
                        request.getRequestURI(), executor.getFileName()));
            }

            log.info(realSqlDefinition.getKey());
            return jdbcTemplate.update(realSqlDefinition.getKey(), ps -> {
                if (ArrayUtil.isNotEmpty(realSqlDefinition.getValue())) {
                    for (int i = 0; i < realSqlDefinition.getValue().length; i++) {
                        ps.setObject(i + 1, realSqlDefinition.getValue()[i]);
                    }
                }
            });
        }
    }

    private Pair<String, Object[]> getSql(String engine, String sqlDefinition, HttpServletRequest request, Object parent) throws Exception {
        String sql;
        Object[] params;
        if (XmlExecutor.ENGINE_JAVASCRIPT.equalsIgnoreCase(engine)) {
            ScriptEngineManager engineManager = new ScriptEngineManager();
            ScriptEngine jsEngine = engineManager.getEngineByName(engine);
            Bindings bindings = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("$request", request);
            bindings.put("$param", RequestUtil.getParamMap(request));
            bindings.put("$header", RequestUtil.getHeaderMap(request));
            bindings.put("$body", RequestUtil.getBodyMap(request));
            bindings.put("$parent", parent);
            bindings.put(DaoConstants.SQL_ASSEMBLY_RESULT, StrUtil.EMPTY);
            bindings.put(DaoConstants.SQL_ASSEMBLY_PARAMETER, new HashMap<>());
            bindings.put(DaoConstants.SQL_ASSEMBLY_BATCH, new HashMap<>());
            jsEngine.eval(sqlDefinition, bindings);
            sql = (String) bindings.getOrDefault(DaoConstants.SQL_ASSEMBLY_RESULT, StrUtil.EMPTY);
            HashMap<Integer, Object> map = (HashMap) bindings.getOrDefault(DaoConstants.SQL_ASSEMBLY_PARAMETER, new Object[0]);
            params = new Object[map.size()];
            for (Map.Entry<Integer, Object> entry : map.entrySet()) {
                params[entry.getKey()] = entry.getValue();
            }

        } else if (XmlExecutor.ENGINE_GROOVY.equalsIgnoreCase(engine)) {
            GroovyShell groovyShell = new GroovyShell();
            Script script = groovyShell.parse(sqlDefinition);
            script.setProperty("$request", request);
            script.setProperty("$param", RequestUtil.getParamMap(request));
            script.setProperty("$header", RequestUtil.getHeaderMap(request));
            script.setProperty("$body", RequestUtil.getBodyMap(request));
            script.setProperty("$parent", parent);
            script.setProperty(DaoConstants.SQL_ASSEMBLY_RESULT, StrUtil.EMPTY);
            script.setProperty(DaoConstants.SQL_ASSEMBLY_PARAMETER, new HashMap<>());
            script.run();
            sql = (String) script.getProperty(DaoConstants.SQL_ASSEMBLY_RESULT);
            HashMap<Integer, Object> map = (HashMap) script.getProperty(DaoConstants.SQL_ASSEMBLY_PARAMETER);
            params = new Object[map.size()];
            for (Map.Entry<Integer, Object> entry : map.entrySet()) {
                params[entry.getKey()] = entry.getValue();
            }
        } else {
            throw new ExecutorException("Invalid executor engine: " + engine);
        }

        return Pair.of(sql, params);
    }

    private Object doDefaultDaoExecutor(XmlExecutor executor, HttpServletRequest request, Object parentParams) {
        String executorRef = executor.getRef();
        if (!executorRef.contains(".")) {
            throw new ExecutorException("Error sql definition in xml file " + executor.getFileName());
        }

        List<String> refArray = StrUtil.split(executorRef, ".");
        if (refArray.size() < 2) {
            throw new ExecutorException("Error sql definition in xml file " + executor.getFileName());
        }

        String daoName = refArray.get(0);
        daoName = StrUtil.isEmpty(executor.getSchema()) ? StrUtil.EMPTY : (executor.getSchema() + '.' + daoName);
        DaoExecutor daoExecutor = executorMap.get(daoName);
        if (null == daoExecutor) {
            throw new ExecutorException("Error sql definition in xml file " + executor.getFileName());
        }

        Object value;
        String daoMethod = refArray.get(1);
        Map<String, Object> valueMap = parameterConverter.convert(executor.getParams(), request, parentParams);
        switch (daoMethod) {
            case "insert" -> value = daoExecutor.insert(valueMap);
            case "deleteById" -> value = daoExecutor.deleteById(valueMap);
            case "updateById" -> value = daoExecutor.updateById(valueMap);
            case "select" -> value = daoExecutor.select(valueMap);
            case "selectOne" -> value = daoExecutor.selectOne(valueMap);
            case "exist" -> value = daoExecutor.exist(valueMap);
            case "count" -> value = daoExecutor.count(valueMap);
            case "selectPage" -> value = daoExecutor.selectPage(valueMap);
            // 只能使用bodyJson
            case "batchInsert" -> {
                JSONArray values = (JSONArray) valueMap.entrySet().iterator().next().getValue();
                if (CollUtil.isNotEmpty(values)) {
                    daoExecutor.batchInsert(values);
                }
                value = true;
            }
            // 只能使用bodyJson
            case "batchUpdate" -> {
                JSONArray values = (JSONArray) valueMap.entrySet().iterator().next().getValue();
                if (CollUtil.isNotEmpty(values)) {
                    daoExecutor.batchUpdate(values);
                }
                value = true;
            }
            default -> value = StrUtil.EMPTY;
        }

        return FieldTypeConverterManager.convert(value);
    }
}
