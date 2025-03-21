package io.github.lucklike.httpclient.configapi;

import com.luckyframework.httpclient.core.ssl.KeyStoreInfo;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.configapi.EnableConfigurationParser;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.interceptor.PriorityConstant;
import com.luckyframework.reflect.Combination;
import io.github.lucklike.httpclient.annotation.HttpClientComponent;
import io.github.lucklike.httpclient.annotation.ProxyModel;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Executors;

import static io.github.lucklike.httpclient.Constant.SPRING_ENV_CONFIG_SOURCE;

/**
 * <b>如下所有配置均支持松散绑定</b><br/><br/>
 * 声明式Http客户端的注解，支持从Spring环境变量中获取请求与响应转化的相关配置<br/>
 *
 * <b>配置内容：</b><br/>
 * <pre>
 *   {@code
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
 *          #指定当前请求为SSE请求
 *          sse: http://localhost:8080/envApi/sse
 *          #指定请求的HTTP方法
 *          method: POST
 *          #指定请求是否异步，仅对void方法生效
 *          async: true/false
 *          #异步模型，支持Java线程模型和Kotlin协程模型
 *          async-model: JAVA_THREAD/KOTLIN_COROUTINE
 *          #指定执行改异步请求的线程池名称，支持Executor、ThreadPoolParam、String类型的返回结果
 *          async-executor: async-pool-name
 *          #最大并发数，配置之后lucky会为当前方法创建一个专用的线程池，此配置的优先级低于async-executor
 *          #Java线程模型下  ：使用{@link Executors#newFixedThreadPool(int)}创建
 *          #Kotlin协程模型下：使用{@link kotlinx.coroutines.Dispatchers#getIO()#limitedParallelism(int)}控制
 *          async-concurrency: 10
 *          #指定连接超时时间
 *          connect-timeout: 10000
 *          #指定读超时时间
 *          read-timeout: 15000
 *          #指定写超时时间
 *          write-timeout: 15000
 *
 *          #使用类型指定执行当前请求的HTTP执行器
 *          http-executor: JDK/HTTP_CLIENT/OK_HTTP
 *
 *          #Mock相关的配置
 *          mock:
 *            #是否开启Mock功能的SpEL表达式
 *            enable: true
 *
 *            #是否将第一次生成的MockResponse缓存下来，默认：true
 *            cache: true
 *
 *            #模式一,优先级最高：使用SpEL表达式配置一个MockResponse类型的响应体
 *            response: "#{#baiduMock()}"
 *
 *            #模式二,优先级其次：配置一个用于生成MockResponse的SpEL函数名，MockResponse对象的生成将交与该函数
 *            mock-func-name: baiduMoc
 *
 *            #模式三，优先级最低：直接配置status、header、body
 *            #Mock HTTP状态码
 *            status: 404
 *            #Mock 响应头
 *            header:
 *              Server: BWS/1.1
 *              X-Xss-Protection: 1;mode=block
 *              Content-Type: text/html; charset=utf-8
 *              X-USER-LIST:
 *                  - USER-JACK
 *                  - USER-TOM
 *                  - USER-KARL
 *            #Mock 响应体表达式，支持的返回值类型：String、byte[]、InputStream、File、Resource、InputStreamSource、ByteBuffer
 *            body: >-
 *              String: Content-Type需要在header中进行配置
 *              byte[]: Content-Type需要在header中进行配置
 *              InputStream: Content-Type需要在header中进行配置
 *              File: Content-Type会自动根据文件获取，并且会设置Content-Disposition
 *              Resource: Content-Type会自动根据文件获取，并且会设置Content-Disposition
 *              InputStreamSource: Content-Type需要在header中进行配置
 *              ByteBuffer: Content-Type需要在header中进行配置
 *
 *          #SSL认证相关配置
 *          ssl:
 *            #开启SSL认证
 *            enable: true
 *            #指定协议
 *            protocol: TLS
 *
 *            #模式一：【优先级最高】使用SpEL表达式指定HostnameVerifier和SSLSocketFactory
 *            hostname-verifier: "#{@myHostnameVerifier}"
 *            ssl-socket-factory: "#{@SSLSocketFactory}"
 *
 *            #模式二：【优先级其次】指定具体的KeyStore信息
 *            #KeyStore配置，用于提供证书给服务器验证
 *            key-store-info:
 *              algorithm: SunX509                                          #KeyManagerFactory算法
 *              cert-password: 123456                                       #cert密码
 *              key-store-type: JKS                                         #KeyStore类型
 *              key-store-file: file:/Users/lucklike/Downloads/keystore.jks #KeyStore公钥文件地址
 *              key-store-password: 23232323                                #KeyStore私钥文件密码
 *            #信任库配置，用于验证服务器提供的证书
 *            trust-store-info:
 *              algorithm: SunX509                                          #KeyManagerFactory算法
 *              key-store-type: JKS                                         #KeyStore类型
 *              key-store-file: file:/Users/lucklike/Downloads/keystore.jks #KeyStore公钥文件地址
 *              key-store-password: 23232323                                #KeyStore私钥文件密码
 *
 *            #模式三：【优先级最低】，使用SpEL表达式来获取一个KeyStoreInfo对象：
 *            # 1.表达式返回结果为{@link String}类型时，会此返回值作为ID通过{@link HttpClientProxyObjectFactory#getKeyStoreInfo(String)}来获取KeyStoreInfo对象
 *            # 2.表达式返回结果为{@link KeyStoreInfo }类型时，则直接使用该对象
 *            #KeyStore配置，用于提供证书给服务器验证
 *            key-store: keyStore-1
 *            #信任库配置，用于验证服务器提供的证书
 *            trust-store: "#{#getTrustStoreInfo()}"
 *
 *
 *          #重定向配置
 *          redirect:
 *            #开启自动重定向功能，默认：false
 *            enable: true
 *            #需要重定向的状态码，默认：301, 302, 303, 304, 307, 308
 *            status: [301, 302, 303, 304, 307, 308]
 *            #允许最大重定向次数，默认：5
 *            max-count: 5
 *            #重定向条件表达式
 *            condition: "#{$status$ == 301}"
 *            #获取重定向地址的表达式，默认：#{$respHeader$.Location}
 *            location: "#{$respHeader$.Location}"
 *            #重定向拦截器的优先级，默认：100
 *            priority: 100
 *
 *          #日志配置
 *          logger:
 *            #是否开启日志功能，默认关闭
 *            enable: true
 *            #是否打印请求日志：默认开启（仅在enable为true时生效）
 *            enable-req-log: true
 *            #是否打印响应日志：默认开启（仅在enable为true时生效）
 *            enable-resp-log: true
 *            #是否开启打印注解信息功能，默认关闭
 *            enable-annotation-log: true
 *            #是否开启打印参数信息功能，默认关闭
 *            enable-args-log: true
 *            #是否强制打印响应体信息
 *            force-print-body: false
 *            #日志打印拦截器的优先级，默认2147483647
 *            priority: 2147483647
 *            #MimeType为这些类型时，将打印响应体日志（覆盖默认值）
 *            #默认值：
 *            #application/json
 *            #application/xml
 *            #application/x-java-serialized-object
 *            #text/xml
 *            #text/plain
 *            #text/html
 *            set-allow-mime-types:
 *              - application/json
 *              - application/xml
 *              - application/x-java-serialized-object
 *              - text/xml
 *              - text/plain
 *              - text/html
 *            #MimeType为这些类型时，将打印响应体日志（在默认值的基础上新增）
 *            #默认值：
 *            #application/json
 *            #application/xml
 *            #application/x-java-serialized-object
 *            #text/xml
 *            #text/plain
 *            #text/html
 *            add-allow-mime-types:
 *              - text/plain
 *              - text/html
 *            #响应体超过该值时，将不会打印响应体日志，值小于等于0时表示没有限制,单位：字节默认值：-1
 *            body-max-length: 100
 *            #打印请求日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
 *            req-log-condition: "#{$status$ != 200}"
 *            #打印响应日志的条件，这里可以写一个返回值为boolean类型的SpEL表达式，true时才会打印日志
 *            resp-log-condition: "#{$status$ != 200}"
 *
 *          retry:
 *            #是否开启重试的开关
 *            enable: true
 *            #任务名称，默认值为{@link MethodContext#getSimpleSignature}
 *            task-name: bilibili-index
 *            #最大重试次数，默认3次
 *            max-count: 5
 *            #重试等待时间，单位：毫秒，默认值1000
 *            wait-millis: 2000
 *            #下一次等待时间与上一次等待时间的比值，默认值：0
 *            multiplier: 2
 *            #最大等待时间，单位：毫秒，默认值：10000
 *            max-wait-millis: 20000
 *            #最小等待时间，单位：毫秒，默认值：500
 *            min-wait-millis: 600
 *            #需要重试的异常列表，默认值为Exception
 *            exception:
 *              - java.lang.Exception
 *            #不需要重试的异常列表，默认值为null
 *            exclude:
 *              - java.lang.RuntimeException
 *            #需要进行重试的HTTP状态码，默认值为null
 *            exception-status: [404, 405, 406, 500]
 *            #不需要进行重试的HTTP状态码，默认值为null
 *            normal-status: [202, 301]
 *            #决定是否需要重试的表达式
 *            expression: "#{$status$ != 200}"
 *            #指定决定是否需要进行重试的SpEL函数
 *            func-name: needRetry
 *
 *          #请求扩展处理器
 *          request-extension:
 *              #配置扩展处理器
 *            - handle:
 *                # 模式一： class-name + scope
 *                class-name: io.github.lucklike.luckyclient.api.mock.UserRequestExtendHandle
 *                scope: SINGLETON/ PROTOTYPE/ METHOD/ CLASS/ METHOD_CONTEXT
 *                #模式二：bean-name
 *                bean-name: userRequestExtendHandle
 *
 *             #配置对象
 *              config:
 *                id: 123
 *                name: JackFu
 *                email: jackFu@qq.com
 *
 *          #使用自定义的HTTP执行器
 *          http-executor-config:
 *            #模式一：指定Spring容器中Bean的名称
 *            bean-name: myHttpExecutor
 *
 *            #模式二：使用Class+Scope方式指定
 *            class-name: io.github.lucklike.springboothttp.api.MyHttpExecutor
 *            scope: SINGLETON/PROTOTYPE/METHOD/CLASS/METHOD_CONTEXT
 *
 *          #在SpEL运行时环境中声明变量和函数
 *          spring-el-import:
 *            #声明Root变量，可以直接通过变量名引用
 *            root:
 *              key: value
 *              key2: "#{key}/test"
 *            #声明普通变量，需要通过#变量名引用
 *            val:
 *              var: value
 *              var2: "#{#var}/test"
 *            #声明Root字面量，不会进行SpEL解析
 *            root-lit:
 *              key1: value1
 *              key2: value2
 *            #声明普通字面量，不会进行SpEL解析
 *            var-lit:
 *              var1: value1
 *              var2: value2
 *            #导入函数集合，此处导入的类中的静态方法都会被导入到SpEL运行时环境中，使用'#方法名(参数)'的方式进行调用
 *            classes:
 *              - com.luckyframework.httpclient.proxy.configapi.EncoderUtils
 *              - com.luckyframework.httpclient.MyUtils
 *            #导入包，调用其中的类的静态方法或者实例化时则可以省略包名，例如：#{new ArrayList()}、#{T(Arrays).toString()}
 *            pack:
 *              - java.util
 *              - com.luckyframework.httpclient
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
 *          multipart-form-data:
 *            #设置文本类型的参数
 *            txt:
 *              name: lucy
 *              sex: 女
 *              age: 25
 *            #设置文件类型的参数
 *            file:
 *              photo: file:D:/user/image/photo.jpg               #可以是本地文件
 *              idCard-1: http://localhost:8888/idCard/lucky.png  #也可以是网路上的文件
 *              idCard-2: "#{p1}"                                 #取参数列表中的第二个参数来得到文件
 *
 *          # 条件请求头
 *          condition-header:
 *              # 条件表达式
 *            - condition: 1==1
 *              # 请求头数据项
 *              data:
 *                c1-h1: hh_11
 *                c1-h2: "@if(1==2): hhh_22"
 *            - condition: 1==1
 *              data:
 *                c2-h1: c2_hh_11
 *                c2-h2: "@if(1==1): c2_qqq_22"
 *
 *          # 条件Query请求参数
 *          condition-query:
 *              # 条件表达式
 *            - condition: 1==1
 *              # Query请求参数项
 *              data:
 *                c1-q1: qq_11
 *                c1-q2: "@if(1==2): qqq_22"
 *            - condition: 1==1
 *              data:
 *                c2-q1: c2_qq_11
 *                c2-q2: "@if(1==2): c2_qqq_22"
 *
 *          # 条件Form请求参数
 *          condition-form:
 *              # 条件表达式
 *            - condition: 1==1
 *              # Form请求参数项
 *              data:
 *                c1-form-1: ff_111111
 *                c1-form-2: "@if(1==2): ff_222222"
 *            - condition: 1==1
 *              data:
 *                c2-form-3: ff_333333
 *                c2-form-4: ff_444444
 *
 *          # 条件Path请求参数
 *          condition-path:
 *            # 条件表达式
 *            - condition: 1==1
 *              # Path请求参数项
 *              data:
 *                c1-path-1: ff_111111
 *                c1-path-2: "@if(1==2): ff_222222"
 *            - condition: 1==1
 *              data:
 *                c2-path-3: ff_333333
 *                c2-path-4: ff_444444
 *
 *          # 条件multipart-form-data请求参数
 *          condition-multipart-form-data:
 *              # 条件表达式
 *            - condition: 1==1
 *              data:
 *                txt:
 *                  t1: "@if(1==2): 1234"
 *                  t2: 2344
 *                  t3: 5555
 *                file:
 *                  file1: "@if(#{$query$.containsKey('c2-q2')}): classpath:api-info.yml"
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
 *            json:
 *              - id: "#{id}"
 *                username: "#{name}"
 *                password: PA$$W0RD
 *                email: "#{name}_#{id}@qq.com"
 *                age: 27
 *              - id: "#{id2}"
 *                username: "#{name2}"
 *                password: #{pwd}
 *                email: "#{name}_#{id}@qq.com"
 *                age: 18
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
 *            #模式六：使用Java序列化请求体
 *            java: #{#java(p0)} #使用java函数将参数列表中的第一个参数进行序列化
 *
 *            #模式七：使用Google Protobuf格式的请求体
 *            protobuf: #{#protobuf(p0)}  #使用protobuf函数将参数列表中的第一个参数转化为Google Protobuf格式的请求体
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
 *          #禁用拦截器配置,在此处配置的拦截器逻辑将不会执行，注意：此处配置的是Interceptor接口实现类中uniqueIdentification()方法的返回值
 *          interceptor-prohibit:
 *            - Interceptor#uniqueIdentification()
 *            - com.luckyframework.httpclient.proxy.interceptor.PrintLogInterceptor
 *
 *          #配置响应转换器，其中result和exception至少要写一个
 *          resp-convert:
 *            #自定义响应转换器处理器
 *            convert:
 *              #自定义配置处理器，
 *              handle:
 *                # 模式一： class-name + scope
 *                class-name: com.luckyframework.httpclient.proxy.configapi.ResponseConvertHandle
 *                scope: SINGLETON/ PROTOTYPE/ METHOD/ CLASS/ METHOD_CONTEXT
 *
 *                #模式二：bean-name
 *                bean-name: userResponseConvertHandle
 *
 *              #自定义配置
 *              config:
 *                id: 1234
 *                name: JackKang
 *                email: jackKang@qq.com
 *
 *            #使用默认的响应转换逻辑
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
 *          #是否禁止使用响应转换器，如果禁止则直接将响应体转化为方法返回值
 *          convert-prohibit: false
 *
 *          #配置SSE请求的监听器
 *          sse-listener:
 *            #模式一：指定Spring容器中Bean的名称
 *            bean-name: myEventListener
 *
 *            #模式二：使用Class+Scope方式指定
 *            class-name: io.github.lucklike.springboothttp.api.sse.MyEventListener
 *            scope: SINGLETON/PROTOTYPE/METHOD/CLASS/METHOD_CONTEXT
 *
 *   }
 * </pre>
 * @author fukang
 * @version 1.0.0
 * @date 2023/8/30 03:06
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@HttpClientComponent
@EnableConfigurationParser(source = "Spring Environment", sourceType = SPRING_ENV_CONFIG_SOURCE)
@Combination({EnableConfigurationParser.class})
public @interface SpringEnvHttpClient {

    /**
     * 定义配置前缀
     */
    @AliasFor(annotation = EnableConfigurationParser.class, attribute = "prefix")
    String prefix() default "";

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = HttpClientComponent.class, attribute = "name")
    String name() default "";

    /**
     * 拦截器优先级，数值越高优先级越低
     */
    @AliasFor(annotation = EnableConfigurationParser.class, attribute = "priority")
    int priority() default PriorityConstant.CONFIG_API_PRIORITY;

    /**
     * 代理模式
     */
    @AliasFor(annotation = HttpClientComponent.class, attribute = "proxyModel")
    ProxyModel proxyModel() default ProxyModel.AUTO;
}
