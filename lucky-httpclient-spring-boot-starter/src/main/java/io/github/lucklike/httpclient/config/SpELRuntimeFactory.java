package io.github.lucklike.httpclient.config;

import com.luckyframework.spel.SpELRuntime;

/**
 * SpEL运行时环境工厂
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/9/9 10:50
 */
@FunctionalInterface
public interface SpELRuntimeFactory {

    SpELRuntime getSpELRuntime();
}
