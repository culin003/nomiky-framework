package org.nomiky.nomikyframework.interceptor;

public interface NomikyInterceptor {

    boolean isBefore();

    void process(InterceptorContext context);

}
