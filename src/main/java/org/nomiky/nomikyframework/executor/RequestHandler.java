/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.executor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomiky.nomikyframework.entity.R;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 处理Controller业务
 *
 * @author nomiky
 * @since 2023年12月25日 14时34分
 */
public interface RequestHandler {

    /**
     * 处理业务
     *
     * @param request
     * @param response
     */
    @ResponseBody
    R<?> handler(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
