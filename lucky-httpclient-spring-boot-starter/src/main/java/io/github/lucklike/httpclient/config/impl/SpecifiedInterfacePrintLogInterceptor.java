package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.Response;
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
        } else {
            initStartTime();
        }

    }

    @Override
    public Response afterExecute(Response response, InterceptorContext context) {
        if (isPrintMethod(context.getContext()) && isPrintResponseLog) {
            return super.afterExecute(response, context);
        } else {
            initEndTime();
        }
        return response;
    }

    private boolean isPrintMethod(MethodContext context) {
        if (ContainerUtils.isEmptyCollection(printLogPackageSet)) {
            return false;
        }
        String className = context.getClassContext().getCurrentAnnotatedElement().getName();
        for (String packagePrefix : printLogPackageSet) {
            if (className.startsWith(packagePrefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String uniqueIdentification() {
        return PrintLogInterceptor.class.getName();
    }
}
