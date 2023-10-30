package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.impl.creator.AbstractCachedObjectCreator;
import com.luckyframework.reflect.ClassUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

/**
 * 对象创建器，如果存在msg配置则使用{@link BeanFactory#getBean(String)}来生成对象
 * 否则使用{@link ClassUtils#newObject(Class, Object...)}的方式来生成对象
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:40
 */
public class BeanObjectCreator extends AbstractCachedObjectCreator {

    private final BeanFactory beanFactory;

    public BeanObjectCreator(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    protected <T> T createObject(Class<T> aClass, String createMessage) {
        if (StringUtils.hasText(createMessage)) {
            return (T) beanFactory.getBean(createMessage, aClass);
        }
        return ClassUtils.newObject(aClass);
    }
}
