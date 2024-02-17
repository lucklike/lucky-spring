package io.github.lucklike.httpclient;

import com.luckyframework.httpclient.proxy.creator.ReflectObjectCreator;
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
public class BeanObjectCreator extends ReflectObjectCreator {

    private final ApplicationContext applicationContext;

    public BeanObjectCreator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected <T> T doCreateObject(Class<T> clazz, String msg) {
        if (StringUtils.hasText(msg)) {
            if (applicationContext.isTypeMatch(msg, clazz)) {
                return applicationContext.getBean(msg, clazz);
            }
            throw new IllegalArgumentException("Component creation failed: The bean name '" + msg + "' and bean type '" + clazz.getName() + "' provided in the creation information do not match each other");
        }
        return super.doCreateObject(clazz, msg);
    }
}
