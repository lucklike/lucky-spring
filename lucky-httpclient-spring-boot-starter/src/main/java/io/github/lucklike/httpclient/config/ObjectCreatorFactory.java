package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.creator.ObjectCreator;
import org.springframework.lang.NonNull;

/**
 * 对象创建器工厂
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/9 09:13
 */
@FunctionalInterface
public interface ObjectCreatorFactory {

    @NonNull
    ObjectCreator getObjectCreator();
}
