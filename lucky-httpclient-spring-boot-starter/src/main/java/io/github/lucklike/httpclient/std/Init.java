package io.github.lucklike.httpclient.std;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 初始化参数绑定注解，需要配合{@link StdHttpClient}注解一起使用
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/7 00:58
 * @see StdInitBindParameterConvert
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Init {

}
