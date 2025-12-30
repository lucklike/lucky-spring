package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.logging.LoggerHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * 指定接口日志打印
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/10/7 00:40
 */
public class SpecifiedInterfaceLoggerHandler implements LoggerHandler {

    private final LoggerHandler delegate;

    private Set<String> printLogPackageSet = new HashSet<>();
    private boolean printRequestLog = true;
    private boolean isPrintResponseLog = true;

    public SpecifiedInterfaceLoggerHandler(LoggerHandler delegate) {
        this.delegate = delegate;
    }

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
    public void recordRequestLog(MethodContext context, Request request) {
        if (isPrintMethod(context) && printRequestLog) {
            delegate.recordRequestLog(context, request);
        }
    }

    @Override
    public void recordMetaResponseLog(MethodContext context, Response response) {
        if (isPrintMethod(context) && isPrintResponseLog) {
            delegate.recordMetaResponseLog(context, response);
        }
    }

    private boolean isPrintMethod(MethodContext context) {
        if (ContainerUtils.isEmptyCollection(printLogPackageSet)) {
            return false;
        }

        // [all] 表示全部
        if (printLogPackageSet.contains("[all]")) {
            return true;
        }

        String className = context.getClassContext().getCurrentAnnotatedElement().getName();
        for (String packagePrefix : printLogPackageSet) {
            if (className.startsWith(packagePrefix)) {
                return true;
            }
        }
        return false;
    }
}
