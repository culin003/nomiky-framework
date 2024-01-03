/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.util.Maps;
import org.nomiky.nomikyframework.constant.DaoConstants;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析参数引用，将参数引用转换成DaoExecutor需要的参数类型，目前，DaoExecutor参数类型已经约定为：Map<String, Object>
 * 默认的参数转换器:
 * 1、从请求URL参数中获取：
 * ${param.xxx}
 * 2、从请求路径参数中获取，需要在controller -> path属性中包含xxx参数：
 * ${path.xxx}
 * 3、从请求body中获取JSON属性：
 * ${bodyJson.xxx}
 * 4、从请求body中获取body字符串：
 * ${bodyString}
 *
 * @author nomiky
 * @since 2023年12月25日 17时00分
 */
@Slf4j
public class SpelParameterConvertor implements ParameterConverter {

    private static final ExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();
    private static final Pattern PATTERN = Pattern.compile("\\[(.*?)\\]");

    @Override
    public Map<String, Object> convert(String paramRef, HttpServletRequest request, Object parentParams) throws IOException {
        paramRef = StrUtil.isEmpty(paramRef) ? DaoConstants.REQUEST_PARAMETER : paramRef;
        Map<String, Object> valuesMap = new HashMap<>();
        if (paramRef.startsWith(DaoConstants.BODY_JSON)) {
            valuesMap = explainBodyJsonParams(request, paramRef);
        } else if (paramRef.startsWith(DaoConstants.BODY_STRING)) {
            String bodyString = IoUtil.read(request.getInputStream(), Charset.defaultCharset());
            valuesMap.put(DaoConstants.BODY_STRING, bodyString);
        } else if (paramRef.startsWith(DaoConstants.PARENT_RESULT)) {
            valuesMap = explainParentParams(parentParams, paramRef);
        } else if (paramRef.startsWith(DaoConstants.REQUEST_HEADER)) {
            valuesMap = explainHeaderParams(request, paramRef);
        } else {
            valuesMap = explainParamParams(request, paramRef);
        }

        return valuesMap;
    }

    private Map<String, Object> explainParamParams(HttpServletRequest request, String paramRef) {
        Map<String, Object> valuesMap = new HashMap<>();
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getParameter(key);
            if (null != value) {
                valuesMap.put(key, value);
            }
        }

        return getParamResult(paramRef, valuesMap, DaoConstants.REQUEST_PARAMETER);
    }

    private Map<String, Object> explainHeaderParams(HttpServletRequest request, String paramRef) {
        Map<String, Object> valuesMap = new HashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getHeader(key);
            if (null != value) {
                valuesMap.put(key, value);
            }
        }

        return getParamResult(paramRef, valuesMap, DaoConstants.REQUEST_PARAMETER);
    }

    private Map<String, Object> explainParentParams(Object parentParams, String paramRef) {
        Map<String, Object> valuesMap = Maps.of(DaoConstants.PARENT_RESULT, parentParams);
        return getParamResult(paramRef, valuesMap, DaoConstants.PARENT_RESULT);
    }

    private Map<String, Object> explainBodyJsonParams(HttpServletRequest request, String paramRef) throws IOException {
        String jsonStr = IoUtil.read(request.getInputStream(), Charset.defaultCharset());
        Map<String, Object> valuesMap = JSONUtil.toBean(jsonStr, JSONObject.class);
        return getParamResult(paramRef, valuesMap, DaoConstants.BODY_JSON);
    }

    private Map<String, Object> getParamResult(String paramRef, Map<String, Object> valuesMap, String paramType) {
        Map<String, Object> resultMap = new HashMap<>();
        // 多个映射
        if (paramRef.contains(StrUtil.COMMA)) {
            List<String> paramRefArray = StrUtil.split(paramRef, StrUtil.COMMA);
            for (String s : paramRefArray) {
                setValueFromSPELExpression(resultMap, valuesMap, s, paramType);
            }
        }
        // 全部映射
        else if (paramRef.equalsIgnoreCase(paramType)) {
            resultMap = valuesMap;
        }
        // 单个映射
        else {
            setValueFromSPELExpression(resultMap, valuesMap, paramRef, paramType);
        }

        return resultMap;
    }


    private void setValueFromSPELExpression(Map<String, Object> resultMap,
                                            Map<String, Object> valuesMap,
                                            String spelExpression,
                                            String paramName) {
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable(paramName, valuesMap);
        Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(spelExpression);
        Matcher matcher = PATTERN.matcher(expression.getExpressionString());
        if (matcher.find()) {
            resultMap.put(matcher.group(1), expression.getValue(context));
        }
    }
}
