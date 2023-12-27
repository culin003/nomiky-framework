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
import org.nomiky.nomikyframework.constant.DaoConstants;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class DefaultParameterConvertor implements ParameterConverter {

    @Override
    public Map<String, Object> convert(String paramRef, HttpServletRequest request, Object parentParams) throws IOException {
        paramRef = StrUtil.isEmpty(paramRef) ? "bodyJson" : paramRef;
        Map<String, Object> valuesMap = new HashMap<>();
        if (paramRef.startsWith("bodyJson")) {
            valuesMap = explainBodyJsonParams(request, paramRef);
        } else if (paramRef.startsWith("bodyString")) {
            String bodyString = IoUtil.read(request.getInputStream(), Charset.defaultCharset());
            valuesMap.put(DaoConstants.BODY_STRING, bodyString);
        } else if (paramRef.startsWith("parent")) {
            valuesMap = explainParentParams(parentParams, paramRef);
        } else {
            valuesMap = explainParamParams(request, paramRef);
        }

        return valuesMap;
    }

    private Map<String, Object> explainParamParams(HttpServletRequest request, String paramRef) {
        Map<String, Object> valuesMap = new HashMap<>();
        // 多个映射
        if (paramRef.contains(",")) {
            List<String> paramRefArray = StrUtil.split(paramRef, ",");
            for (String s : paramRefArray) {
                if (!s.contains(".")) {
                    continue;
                }

                List<String> paramArray = StrUtil.split(s, ".");
                String key = paramArray.get(0);
                String value = paramArray.get(1);
                if (key.startsWith("param")) {
                    valuesMap.put(value, request.getParameter(value));
                }
            }
        }
        // 单个映射
        else if (paramRef.contains(".")) {
            List<String> paramArray = StrUtil.split(paramRef, ".");
            String key = paramArray.get(0);
            String value = paramArray.get(1);
            if (key.startsWith("bodyJson")) {
                valuesMap.put(value, request.getParameter(value));
            }
        }
        // 全部映射
        else {
            Enumeration<String> enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getParameter(key);
                valuesMap.put(key, value);
            }
        }

        return valuesMap;
    }

    private Map<String, Object> explainParentParams(Object parentParams, String paramRef) {
        Map<String, Object> valuesMap = new HashMap<>();
        // 多个映射
        if (paramRef.contains(",")) {
            List<String> paramRefArray = StrUtil.split(paramRef, ",");
            for (String s : paramRefArray) {
                if (!s.contains(".")) {
                    continue;
                }

                List<String> paramArray = StrUtil.split(s, ".");
                String key = paramArray.get(0);
                String value = paramArray.get(1);
                if (key.startsWith("param")) {
                    valuesMap.put(value, ((Map<String, Object>) parentParams).get(value));
                }
            }
        }
        // 单个映射
        else if (paramRef.contains(".")) {
            List<String> paramArray = StrUtil.split(paramRef, ".");
            String key = paramArray.get(0);
            String value = paramArray.get(1);
            if (key.startsWith("bodyJson")) {
                valuesMap.put(value, ((Map<String, Object>) parentParams).get(value));
            }
        }
        // 全部映射
        else {
            valuesMap.put(DaoConstants.PARENT_RESULT, parentParams);
        }

        return valuesMap;
    }

    private Map<String, Object> explainBodyJsonParams(HttpServletRequest request, String paramRef) throws IOException {
        Map<String, Object> valuesMap = new HashMap<>();
        String jsonStr = IoUtil.read(request.getInputStream(), Charset.defaultCharset());
        JSONObject jsonObject = JSONUtil.toBean(jsonStr, JSONObject.class);
        // 多个映射
        if (paramRef.contains(",")) {
            List<String> paramRefArray = StrUtil.split(paramRef, ",");
            for (String s : paramRefArray) {
                if (!s.contains(".")) {
                    continue;
                }

                List<String> paramArray = StrUtil.split(s, ".");
                String key = paramArray.get(0);
                String value = paramArray.get(1);
                if (key.startsWith("bodyJson")) {
                    valuesMap.put(StrUtil.toUnderlineCase(value), jsonObject.get(value));
                }
            }
        }
        // 单个映射
        else if (paramRef.contains(".")) {
            List<String> paramArray = StrUtil.split(paramRef, ".");
            String key = paramArray.get(0);
            String value = paramArray.get(1);
            if (key.startsWith("bodyJson")) {
                valuesMap.put(StrUtil.toUnderlineCase(value), jsonObject.get(value));
            }
        }
        // 全部映射
        else {
            jsonObject.forEach((k, v) -> valuesMap.put(StrUtil.toUnderlineCase(k), v));
        }

        return valuesMap;
    }
}
