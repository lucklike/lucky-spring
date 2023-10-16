package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.spel.SpELRuntime;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.config.SpELRuntimeFactory;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 07:08
 */
public class BeanSpELRuntimeFactoryFactory implements SpELRuntimeFactory {

    @Override
    public SpELRuntime getSpELRuntime() {
        return new io.github.lucklike.httpclient.SpELRuntimeFactory(ApplicationContextUtils.getApplicationContext()).createSpELRuntime() ;
    }
}
