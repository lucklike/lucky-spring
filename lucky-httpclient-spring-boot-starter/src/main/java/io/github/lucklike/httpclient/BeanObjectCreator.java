package io.github.lucklike.httpclient;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.httpclient.proxy.creator.ReflectObjectCreator;
import com.luckyframework.reflect.ClassUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象创建器，如果存在msg配置则使用{@link BeanFactory#getBean(String)}来生成对象
 * 否则使用{@link ClassUtils#newObject(Class, Object...)}的方式来生成对象
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:40
 */
@SuppressWarnings("unchecked")
public class BeanObjectCreator extends ReflectObjectCreator {

    private final ApplicationContext applicationContext;

    public BeanObjectCreator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected <T> T doCreateObject(Class<T> clazz, String msg) {
        boolean hasClass = clazz != null && clazz != Void.class;
        boolean hasName = StringUtils.hasText(msg);

        if (hasName) {
            return hasClass
                    ? applicationContext.getBean(msg, clazz)
                    : (T) applicationContext.getBean(msg);
        } else {
            return createObjectByClass(clazz);
        }

    }


    /**
     * 使用Class来创建实例
     * <pre>
     *     1.尝试从Spring容器中获取Bean对象
     *     2.使用反射创建对象
     * </pre>
     *
     * @param clazz Class
     * @return Class的实例对象
     */
    private <T> T createObjectByClass(Class<T> clazz) {
        String[] beanNames = applicationContext.getBeanNamesForType(clazz);
        List<String> matchedBeanNames = new ArrayList<>();
        List<T> mathBeans = new ArrayList<>();
        for (String beanName : beanNames) {
            T bean = applicationContext.getBean(beanName, clazz);
            if (bean.getClass() == clazz) {
                mathBeans.add(bean);
                matchedBeanNames.add(beanName);
            }
        }

        // Spring容器中不存在该类型的 Bean
        if (ContainerUtils.isEmptyCollection(mathBeans)) {
            return super.doCreateObject(clazz);
        }

        // Spring容器中有且只有一个符合的 Bean
        if (mathBeans.size() == 1) {
            return mathBeans.get(0);
        }

        throw new BeanCreationException("There are multiple Beans of type ['" + clazz.getSimpleName() + "'] in the Spring container: " + matchedBeanNames);
    }
}
