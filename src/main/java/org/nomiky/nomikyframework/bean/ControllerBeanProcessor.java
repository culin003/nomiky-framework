/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.nomiky.nomikyframework.entity.R;
import org.nomiky.nomikyframework.entity.XmlController;
import org.nomiky.nomikyframework.entity.XmlExecutor;
import org.nomiky.nomikyframework.entity.XmlMapper;
import org.nomiky.nomikyframework.executor.DaoExecutor;
import org.nomiky.nomikyframework.executor.ParameterConverter;
import org.nomiky.nomikyframework.executor.RequestHandler;
import org.nomiky.nomikyframework.interceptor.InterceptorContext;
import org.nomiky.nomikyframework.interceptor.NomikyInterceptor;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public ControllerBeanProcessor() {
    }

    public void mapHandlerMapping() throws NoSuchMethodException {
        if (CollUtil.isEmpty(controllers)) {
            return;
        }

        for (XmlController controller : controllers) {
            registerMapping(controller);
        }
    }

    private void registerMapping(XmlController controller) throws NoSuchMethodException {
        RequestMappingInfo mappingInfo = RequestMappingInfo.paths(controller.getPath())
                .methods(RequestMethod.resolve(StrUtil.emptyToDefault(controller.getMethod(), "GET").toUpperCase()))
                .consumes(StrUtil.isEmpty(controller.getConsume()) ? new String[0] : new String[]{controller.getConsume()})
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
            //boolean isUseTransaction = controller.getUseTransaction();
            // TODO: 事务处理

            List<XmlExecutor> executors = controller.getExecutors();
            if (CollUtil.isEmpty(executors)) {
                return R.fail("Can not find DaoExecutor for controller mapping: " + controller.getPath());
            }

            // 置前拦截器
            InterceptorContext context = new InterceptorContext(request, response);
            List<NomikyInterceptor> beforeInterceptors = controller.getBeforeInterceptors();
            if (CollUtil.isNotEmpty(beforeInterceptors)) {
                for (NomikyInterceptor beforeInterceptor : beforeInterceptors) {
                    beforeInterceptor.process(context);
                }
            }

            // 处理Executor
            Object value = StrUtil.EMPTY;
            for (XmlExecutor executor : executors) {
                value = doExecutor(executor, request, value);
            }

            // 后置拦截器
            context.setValue(value);
            List<NomikyInterceptor> afterInterceptors = controller.getAfterInterceptors();
            if (CollUtil.isNotEmpty(afterInterceptors)) {
                for (NomikyInterceptor afterInterceptor : afterInterceptors) {
                    afterInterceptor.process(context);
                }
            }

            return R.data(value);
        };
    }

    private Object doExecutor(XmlExecutor executor, HttpServletRequest request, Object parentParams) throws IOException {
        String executorRef = executor.getRef();
        if (!executorRef.contains(".")) {
            return null;
        }

        List<String> refArray = StrUtil.split(executorRef, ".");
        if (refArray.size() < 2) {
            return null;
        }

        String daoName = refArray.get(0);
        DaoExecutor daoExecutor = executorMap.get(daoName);
        if (null == daoExecutor) {
            return null;
        }

        Object value;
        String daoMethod = refArray.get(1);
        Map<String, Object> valueMap = parameterConverter.convert(executor.getParams(), request, parentParams);
        switch (daoMethod) {
            case "insert"     -> value = daoExecutor.insert(valueMap);
            case "deleteById" -> value = daoExecutor.deleteById(valueMap);
            case "updateById" -> value = daoExecutor.updateById(valueMap);
            case "select"     -> value = daoExecutor.select(valueMap);
            case "selectOne"  -> value = daoExecutor.selectOne(valueMap);
            case "exist"      -> value = daoExecutor.exist(valueMap);
            case "count"      -> value = daoExecutor.count(valueMap);
            case "selectPage" -> value = daoExecutor.selectPage(valueMap);
            default           -> value = StrUtil.EMPTY;
        }

        return value;
    }
}
