package io.github.lucklike.httpclient.config.impl;

import com.luckyframework.httpclient.proxy.ObjectCreator;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import io.github.lucklike.httpclient.BeanObjectCreator;
import io.github.lucklike.httpclient.config.ObjectCreatorFactory;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/10 07:08
 */
public class BeanObjectCreatorFactory implements ObjectCreatorFactory {
    @Override
    public ObjectCreator getObjectCreator() {
        return new BeanObjectCreator(ApplicationContextUtils.getApplicationContext());
    }
}
