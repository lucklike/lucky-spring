package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.proxy.creator.Scope;

import java.util.function.Consumer;

/**
 * 生成器实体对象
 *
 * @author fukang
 * @version 1.0.0
 * @date 2024/2/17 14:49
 */
public class GenerateEntry<T> {

    /**
     * 生成器需要生成对象的Class
     */
    private Class<T> type;
    /**
     * 生成该对象所需要的额外信息
     */
    private String msg = "";
    /**
     * 该对象的作用域
     */
    private Scope scope = Scope.SINGLETON;
    /**
     * 对象消费者Class，用于封装设置对象属性的逻辑
     */
    private Class<? extends Consumer<?>> consumerClass = DefaultObjectConsumer.class;

    /**
     * 生成对象的Class
     *
     * @return 生成对象的Class
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * 设置生成对象的Class
     *
     * @param type 生成对象的Class
     */
    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     * 获取生成对象需要用到的额外信息
     *
     * @return 生成对象需要用到的额外信息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置生成对象需要用到的额外信息
     *
     * @param msg 生成对象需要用到的额外信息
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 对象的作用域
     *
     * @return 对象的作用域
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * 设置对象的作用域
     *
     * @param scope 对象的作用域
     */
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    /**
     * 对象消费者Class，用于封装设置对象属性的逻辑
     *
     * @return 对象消费者Class
     */
    public Class<? extends Consumer<?>> getConsumerClass() {
        return consumerClass;
    }

    /**
     * 设置对象消费者Class，用于封装设置对象属性的逻辑
     *
     * @param consumerClass 对象消费者Class
     */
    public void setConsumerClass(Class<? extends Consumer<T>> consumerClass) {
        this.consumerClass = consumerClass;
    }

    /**
     * 默认的消费者（啥也不做）
     */
    public static class DefaultObjectConsumer implements Consumer<Object> {

        @Override
        public void accept(Object o) {

        }
    }
}
