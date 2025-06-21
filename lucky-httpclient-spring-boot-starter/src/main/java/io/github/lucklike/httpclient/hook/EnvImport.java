package io.github.lucklike.httpclient.hook;

import com.luckyframework.httpclient.proxy.spel.hook.Hook;
import com.luckyframework.httpclient.proxy.spel.hook.Lifecycle;
import com.luckyframework.httpclient.proxy.spel.hook.callback.ValueStore;
import com.luckyframework.httpclient.proxy.spel.hook.callback.VarType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ValueStore(storeOrNot = true)
@Hook(hookHandleClass = EnvImportHookHandler.class)
public @interface EnvImport {

    /**
     * 是否启用该回调
     */
    @AliasFor(annotation = Hook.class, attribute = "enable")
    String enable() default "";

    /**
     * 发生异常时是否中断后续流程
     */
    @AliasFor(annotation = Hook.class, attribute = "errorInterrupt")
    boolean errorInterrupt() default true;

    /**
     * 回调执行时机
     */
    @AliasFor(annotation = Hook.class, attribute = "lifecycle")
    Lifecycle lifecycle();

    /**
     * 存储名称，此项不进行配置时，结果将存储到变量"$"中，
     * 例如 String test()方法 --> 方法的运行结果将存储到$test变量中
     */
    @AliasFor(annotation = ValueStore.class, attribute = "name")
    String storeName() default "$";

    /**
     * 存储类型，存为普通变量还是Root变量，默认存储为ROOT变量
     */
    @AliasFor(annotation = ValueStore.class, attribute = "type")
    VarType storeType() default VarType.ROOT;

    /**
     * 是否将结果展开
     * 如果方法返回值为对象或者Map时需要将结果展开之后存储时可以将此属性设置为true
     */
    @AliasFor(annotation = ValueStore.class, attribute = "unfold")
    boolean unfold() default false;

    /**
     * 是否为字面量存储
     */
    @AliasFor(annotation = ValueStore.class, attribute = "literal")
    boolean literal() default false;



}
