/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.nomiky.nomikyframework.constant.DaoConstants;
import org.nomiky.nomikyframework.exception.ExecutorException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nomiky
 * @since 2024年01月04日 15时10分
 */
public class RequestUtil {

    public static Map<String, Object> getHeaderMap(HttpServletRequest request) {
        Map<String, Object> valuesMap = new HashMap<>();
        // 此时header名称将会变成小写
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getHeader(key);
            if (null != value) {
                valuesMap.put(key, value);
            }
        }

        return valuesMap;
    }


    public static Map<String, Object> getParamMap(HttpServletRequest request) {
        Map<String, Object> valuesMap = new HashMap<>();
        // 此时header名称将会变成小写
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getParameter(key);
            if (null != value) {
                valuesMap.put(key, value);
            }
        }

        return valuesMap;
    }

    public static Map<String, Object> getBodyMap(HttpServletRequest request) {
        try {
            String jsonStr = IoUtil.read(request.getInputStream(), Charset.defaultCharset());
            if (StrUtil.isEmpty(jsonStr) || !(StrUtil.startWith(jsonStr, "[") || StrUtil.startWith(jsonStr, "{"))) {
                return new HashMap<>(0);
            }

            return JSONUtil.toBean(jsonStr, JSONObject.class);
        } catch (IOException e) {
            throw new ExecutorException("Get parameter from request body fail!", e);
        }
    }

    public static Map<String, Object> getBodyStringMap(HttpServletRequest request) {
        try {
            String jsonStr = IoUtil.read(request.getInputStream(), Charset.defaultCharset());
            return Map.of(DaoConstants.BODY_STRING, jsonStr);
        } catch (IOException e) {
            throw new ExecutorException("Get parameter from request body fail!", e);
        }
    }
}
