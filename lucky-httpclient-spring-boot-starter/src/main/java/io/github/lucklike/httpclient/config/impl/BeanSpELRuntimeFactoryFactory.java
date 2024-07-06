package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.spel.SpELRuntime;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.BeanEvaluationContextFactory;
import io.github.lucklike.httpclient.config.SpELRuntimeFactory;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 07:08
 */
public class BeanSpELRuntimeFactoryFactory implements SpELRuntimeFactory {

    @Override
    public SpELRuntime getSpELRuntime() {
        BeanEvaluationContextFactory contextFactory = new BeanEvaluationContextFactory(ApplicationContextUtils.getApplicationContext());
        return new SpELRuntime(contextFactory);
    }
}
