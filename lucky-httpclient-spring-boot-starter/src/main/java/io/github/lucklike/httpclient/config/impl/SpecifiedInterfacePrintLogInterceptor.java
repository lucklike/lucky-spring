package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.core.Request;
import com.luckyframework.httpclient.core.Response;
import com.luckyframework.httpclient.proxy.MethodContext;
import com.luckyframework.httpclient.proxy.impl.interceptor.PrintLogInterceptor;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * 指定接口日志打印
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/10/7 00:40
 */
public class SpecifiedInterfacePrintLogInterceptor extends PrintLogInterceptor {

    private Set<String> printLogPackageSet = new HashSet<>();
    private boolean printRequestLog = true;
    private boolean isPrintResponseLog = true;

    public void setPrintLogPackageSet(Set<String> printLogPackageSet) {
        this.printLogPackageSet = printLogPackageSet;
    }

    public void setPrintRequestLog(boolean printRequestLog) {
        this.printRequestLog = printRequestLog;
    }

    public void setPrintResponseLog(boolean printResponseLog) {
        isPrintResponseLog = printResponseLog;
    }

    @Override
    public void requestProcess(Request request, MethodContext context, Annotation requestAfterHandleAnn) {
        if (isPrintMethod(context) && printRequestLog) {
            super.requestProcess(request, context, requestAfterHandleAnn);
        }
    }

    @Override
    public void responseProcess(Response response, MethodContext context, Annotation responseInterceptorHandleAnn) {
        if (isPrintMethod(context) && isPrintResponseLog) {
            super.responseProcess(response, context, responseInterceptorHandleAnn);
        }
    }

    private boolean isPrintMethod(MethodContext context) {
        if (ContainerUtils.isEmptyCollection(printLogPackageSet)) {
            return false;
        }
        String className = ((Class<?>) context.getParentContext().getCurrentAnnotatedElement()).getName();
        for (String packagePrefix : printLogPackageSet) {
            if (className.startsWith(packagePrefix)) {
                return true;
            }
        }
        return false;
    }
}
