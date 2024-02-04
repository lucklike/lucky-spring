package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.creator.CachedReflectObjectCreator;
import com.luckyframework.reflect.ClassUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

/**
 * 对象创建器，如果存在msg配置则使用{@link BeanFactory#getBean(String)}来生成对象
 * 否则使用{@link ClassUtils#newObject(Class, Object...)}的方式来生成对象
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:40
 */
public class BeanObjectCreator extends CachedReflectObjectCreator {

    private final ApplicationContext applicationContext;

    public BeanObjectCreator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected <T> T createObject(Class<T> aClass, String createMessage) {
        if (StringUtils.hasText(createMessage)) {
            if (applicationContext.isTypeMatch(createMessage, aClass)) {
                return applicationContext.getBean(createMessage, aClass);
            }
            throw new IllegalArgumentException("Component creation failed: The bean name '" + createMessage + "' and bean type '" + aClass.getName() + "' provided in the creation information do not match each other");
        }
        return super.createObject(aClass, createMessage);
    }
}
