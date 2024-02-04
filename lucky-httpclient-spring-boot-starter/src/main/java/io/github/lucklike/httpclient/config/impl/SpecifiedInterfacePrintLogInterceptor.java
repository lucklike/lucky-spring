package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.core.Request;
import com.luckyframework.httpclient.core.Response;
import com.luckyframework.httpclient.core.ResponseProcessor;
import com.luckyframework.httpclient.core.VoidResponse;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.interceptor.InterceptorContext;
import com.luckyframework.httpclient.proxy.interceptor.PrintLogInterceptor;

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
    public void beforeExecute(Request request, InterceptorContext context) {
        if (isPrintMethod(context.getContext()) && printRequestLog) {
            super.beforeExecute(request, context);
        }

    }

    @Override
    public VoidResponse afterExecute(VoidResponse voidResponse, ResponseProcessor responseProcessor, InterceptorContext context) {
        if (isPrintMethod(context.getContext()) && isPrintResponseLog) {
            return super.afterExecute(voidResponse, responseProcessor, context);
        }
        return voidResponse;

    }

    @Override
    public Response afterExecute(Response response, InterceptorContext context) {
        if (isPrintMethod(context.getContext()) && isPrintResponseLog) {
            return super.afterExecute(response, context);
        }
        return response;
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
