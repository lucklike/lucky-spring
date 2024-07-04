package io.github.lucklike.httpclient.extend;

import com.luckyframework.httpclient.proxy.annotations.HttpRequest;
import com.luckyframework.httpclient.proxy.annotations.InterceptorRegister;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.ResultConvert;
import com.luckyframework.httpclient.proxy.annotations.StaticParam;
import com.luckyframework.httpclient.proxy.creator.Scope;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import com.luckyframework.reflect.Combination;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 声明式Http客户端的注解，支持从Spring环境变量中获取请求与响应转化的相关配置<br/>
 * 详细配置如下：
 * <pre>
 *   {@code
 *      该注解使用{@link SpELImport}默认导入了{@link EncoderUtils}工具类中的如下方法：
 *      1.base64(String)              -> base64编码函数                    -> #{#base64('abcdefg')}
 *      2.basicAuth(String, String)   -> basicAuth编码函数                 -> #{#basicAuth('username', 'password‘)}
 *      3.url(String)                 -> URLEncoder编码(UTF-8)            -> #{#url('string')}
 *      4.urlCharset(String, String)  -> URLEncoder编码(自定义编码方式)      -> #{#urlCharset('string', 'UTF-8')}
 *
 *      #某个被@EnableEnvironmentClient注解标注的Java接口
 *      顶层的key需要与@EnableEnvironmentClient注解的prefix属性值一致，如果注解没有配置prefix，则key使用接口的全类名
 *      io.github.lucklike.httpclient.EnvTestApi:
 *        #公共配置可以写在此处
 *        ........
 *        url: ~
 *        method: ~
 *        ........
 *
 *        #接口中对应方法的方法名
 *        httpTest:
 *          #指定请求的URL
 *          url: http://localhost:8080/envApi/{version}/test
 *          #指定请求的HTTP方法
 *          method: POST
 *          #指定连接超时时间
 *          connect-timeout: 10000
 *          #指定读超时时间
 *          read-timeout: 15000
 *          #指定写超时时间
 *          write-timeout: 15000
 *
 *          #指定请求头参数
 *          header:
 *            X-USER-TOKEN: ugy3i978yhiuh7y76t709-0u87y78g76
 *            X-USER-ID: 10000
 *            X-USER-NAME: lucklike
 *
 *          #指定Query参数
 *          query:
 *            _time: 1719985901194
 *            type: "#{p0}"     #取去参数列表的第一个参数值
 *            name: "#{name}"   #取去参数列表中名称为name的参数
 *            array:
 *              - array-test-1
 *              - array-test-2
 *
 *          #指定Form表单参数
 *          form:
 *            id: 1234
 *            userName: Jack
 *            email: jack@qq.com
 *            password: "#{#SM4('${privateKey}')}" #使用SpEL运行时环境中的SM4函数对环境中的${privateKey}进行解密
 *
 *          #指定路径参数，用于填充URL中的{}占位符
 *          path:
 *            version: v4 #URL中的{version}部分最终将会被替换为v4
 *            name: value
 *
 *          #使用multipart/form-data格式的文本参数
 *          multi-data:
 *            name: lucy
 *            sex: 女
 *            age: 25
 *
 *          #使用multipart/form-data格式的文件参数
 *          multi-file:
 *            photo: file:D:/user/image/photo.jpg               #可以是本地文件
 *            idCard-1: http://localhost:8888/idCard/lucky.png  #也可以是网路上的文件
 *            idCard-2: "#{p1}"                                 #取参数列表中的第二个参数来得到文件
 *
 *          #配置代理
 *          proxy:
 *            type: SOCKS         #代理类型，目前支持SOCKS和HTTP两种类型，默认值为HTTP
 *            ip: 192.168.0.111   #代理服务器IP，必填参数，不填整体将不生效
 *            port: 8080          #代理服务器端口，必填参数，不填整体将不生效
 *            username: username  #用户名
 *            password: password  #密码
 *
 *          #用来指定请求体参数
 *          body:
 *            #模式一：自定义请求体格式
 *            mime-type: text/plain
 *            charset: UTF-8
 *            data: >
 *              文本数据，可以是任何形式的文本
 *              111111,222222,333333,444
 *              AAAAAA,BBBBBB,CCCCCC,DDD
 *
 *            #模式二：使用JSON格式请求体
 *            json: >
 *              {
 *                 "id": #{id},
 *                 "username": "#{name}",
 *                 "password": "PA$$W0RD",
 *                 "email": "#{name}_#{id}@qq.com",
 *                 "age": 27
 *              }
 *
 *            #模式三：使用XML格式请求体
 *            xml: >
 *              <User>
 *                <id>#{id}</id>
 *                <username>#{name}</username>
 *                <password>PA$$W0RD</password>
 *                <email>#{name}_#{id}@qq.com</email>
 *                <age>27</age>
 *              </User>
 *
 *            #模式四：使用form-url格式的请求体
 *            form: >
 *              id=#{id}&
 *              username=#{name}&
 *              password=PA$$W0RD&
 *              email=#{name}_#{id}@qq.com&
 *              age=27
 *
 *            #模式五：使用二进制请求体
 *            file: file:D:/user/image/photo.jpg
 *
 *          #配置拦截器
 *          interceptor:
 *            #模式一：指定Spring容器中Bean的名称
 *            - bean-name: fanYiGouTokenInterceptor
 *              priority: 1 #优先级，数值越小优先级越高
 *
 *            #模式二：使用Class+Scope方式指定
 *            - class-name: io.github.lucklike.springboothttp.api.FanYiGouApi$TokenInterceptor
 *              scope: SINGLETON/PROTOTYPE/METHOD/CLASS/METHOD_CONTEXT
 *              priority: 2 #优先级，数值越小优先级越高
 *
 *          #配置响应转换器，其中result和exception至少要写一个
 *          resp-convert:
 *            result: "#{$body$.data}"      #响应转换表达式
 *            meta-type: java.lang.Object   #响应转化元数据类型，整个原始响应体对应的Java数据类型
 *            exception: "出异常了老铁，http-status: #{$status$}" #异常表达式，这里可以是一个字符串，可以以是一个异常实例
 *
 *            #条件表达式，其中result和exception至少要写一个
 *            condition:
 *              - assertion: "#{$status$ == 403}" #断言表达式，如果此处返回true则会执行下面的result表达式或者exception表达式
 *                exception: "认证失败，http-status: #{$status$}" #异常表达式，这里可以是一个字符串，可以以是一个异常实例
 *
 *              - assertion: "#{$status$ == 200}"
 *                result: "#{$body$.data}"
 *
 *   }
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:06
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@InterceptorRegister(
        intercept = @ObjectGenerate(clazz = EnvironmentApiFunctionalSupport.class, scope = Scope.CLASS),
        priority = 9000
)
@ResultConvert(
        convert = @ObjectGenerate(clazz = EnvironmentApiFunctionalSupport.class, scope = Scope.CLASS))
@StaticParam(
        resolver = @ObjectGenerate(clazz = EnvironmentApiFunctionalSupport.class, scope = Scope.CLASS),
        setter = @ObjectGenerate(EnvironmentApiParameterSetter.class)
)
@HttpRequest
@SpELImport(fun = {EncoderUtils.class})
@Combination({StaticParam.class, InterceptorRegister.class})
public @interface EnableEnvironmentClient {

    /**
     * 定义配置前缀
     */
    String prefix() default "";
}