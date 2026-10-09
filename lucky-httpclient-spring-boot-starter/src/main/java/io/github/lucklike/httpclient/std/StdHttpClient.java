package io.github.lucklike.httpclient.std;

import com.luckyframework.httpclient.generalapi.plugin.cache.CachePluginMeta;
import com.luckyframework.httpclient.proxy.annotations.ExceptionHandle;
import com.luckyframework.httpclient.proxy.annotations.HttpRequest;
import com.luckyframework.httpclient.proxy.annotations.ObjectGenerate;
import com.luckyframework.httpclient.proxy.annotations.RespConvert;
import com.luckyframework.httpclient.proxy.configapi.ApiConfig;
import com.luckyframework.httpclient.proxy.generator.GeneratedResponseJavaBeanFunction;
import com.luckyframework.httpclient.proxy.mock.Mock;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import io.github.lucklike.httpclient.discovery.HttpClient;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标准HTTP客户端注解（配置驱动型声明式HTTP接口）
 *
 * <p>将本注解标注在接口（或自定义注解）上，即可将接口声明为由配置驱动的HTTP代理接口：
 * 接口方法上无需再声明任何请求相关的注解，服务地址、请求方法、超时时间、请求参数、请求体、
 * SSL、重试、Mock、缓存、响应转换、异常处理等所有信息均通过配置文件提供，配置的根路径为：
 * <pre>{@code
 *     lucky.http-client.standard-client-configs.{configId}
 * }</pre>
 *
 * <p>本注解是一个组合注解，由以下注解组合而成，各项功能均由{@link StdHttpClientFunction}与配置桥接：
 * <pre><code>
 *     {@link ApiConfig @ApiConfig}               定义配置ID（configId），配置ID相同的接口共享同一套配置
 *     {@link HttpRequest @HttpRequest}           将接口声明为HTTP请求接口
 *     {@link HttpClient @HttpClient}             将接口声明为HTTP代理组件（服务地址由配置提供）
 *     {@link Mock @Mock}                         支持通过配置进行Mock
 *     {@link CachePluginMeta @CachePluginMeta}   支持通过配置开启缓存
 *     {@link RespConvert @RespConvert}           支持通过配置进行响应转换
 *     {@link ExceptionHandle @ExceptionHandle}   支持通过配置进行异常处理
 *     {@link SpELImport @SpELImport}             导入标准客户端相关的SpEL函数（{@link StdHttpClientFunction}）
 * </code></pre>
 *
 * <h2>一、快速开始</h2>
 *
 * <p><b>第1步：</b>在Spring Boot启动类或配置类上添加{@code @EnableLuckyHttpClient}
 * （或{@code @EnableLuckyHttpAutoScan}）开启HTTP组件扫描（当启动类所在包未覆盖接口所在包时需指定扫描包）：
 * <pre><code>
 * &#64;SpringBootApplication
 * &#64;EnableLuckyHttpClient(basePackages = "com.example.api")
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * </code></pre>
 *
 * <p><b>第2步：</b>使用{@code @StdHttpClient}声明接口（接口可以是一个完全无实现的接口）：
 * <pre><code>
 * &#64;StdHttpClient(configId = "GiteeApi", path = "/api/v5")
 * public interface GiteeApi {
 *
 *     // 获取指定用户的信息
 *     UserInfo getUserInfo(String username);
 *
 *     // 获取指定用户的仓库列表
 *     List&lt;UserRepo&gt; getUserRepos(String username, int page);
 * }
 * </code></pre>
 *
 * <p><b>第3步：</b>在application.yml中添加配置（url与service必须至少配置一个）：
 * <pre>{@code
 * lucky:
 *   http-client:
 *     standard-client-configs:
 *       GiteeApi:                                  # key为configId，默认取接口的简单类名
 *         url: https://gitee.com                   # 服务地址
 *         method: GET                              # 请求方法，默认POST
 *         path: /users/{username}                  # 接口路径，{name}占位符由path-params填充
 *         path-params:
 *           username: "#{#username}"               # 引用方法参数username（需添加-parameters编译参数）
 *         read-timeout: 10000                      # 读取超时时间，单位毫秒
 *         method-configs:                          # 方法级配置，key为方法名或@Api注解的值
 *           getUserRepos:
 *             path: /repos
 *             query-params:
 *               page: "#{#page}"
 * }</pre>
 * 本例中调用{@code getUserInfo("lucky")}最终请求的URL为{@code https://gitee.com/api/v5/users/lucky}，
 * 由url + 注解path + 配置path逐级拼接而成。
 *
 * <p><b>第4步：</b>注入接口，像调用本地方法一样发起HTTP请求：
 * <pre><code>
 * &#64;Resource
 * private GiteeApi giteeApi;
 *
 * public void test() {
 *     UserInfo user = giteeApi.getUserInfo("lucky");
 * }
 * </code></pre>
 *
 * <h2>二、注解属性说明</h2>
 * <pre>{@code
 * configId           配置ID，默认取接口的简单类名。配置ID相同的接口共享同一套配置，
 *                    对应配置路径：lucky.http-client.standard-client-configs.{configId}
 * beanId             代理对象在Spring容器中的Bean名称，默认取接口简单类名首字母小写后的名称
 * path               全局路径前缀（支持SpEL表达式），会拼接在基础URL之后
 * lifecycle          生命周期管理器Class
 * lifecycleBean      生命周期管理器Bean的名称（从Spring容器中获取）
 * lifecycleGenerate  生命周期管理器对象生成器
 * }</pre>
 *
 * <p>生命周期管理器（{@link LifeCycleManager}）的解析优先级为：配置{@code lifecycle-manager} &gt;
 * {@code lifecycleGenerate} &gt; {@code lifecycleBean} &gt; {@code lifecycle}；
 * 全部缺省时最终会使用默认的{@link StandardLifeCycleManager}（由{@code lifecycle}的默认值提供）。
 *
 * <h2>三、配置详情</h2>
 *
 * <p>所有配置项均使用kebab-case命名（即Java属性{@code readTimeout}对应配置项{@code read-timeout}）。
 * 配置分为两个层级：
 * <pre>{@code
 * 类级别（客户端级别）：lucky.http-client.standard-client-configs.{configId}.*
 * 方法级别：            lucky.http-client.standard-client-configs.{configId}.method-configs.{apiId}.*
 * }</pre>
 * 其中{@code apiId}默认为接口的方法名，也可通过方法上的{@code @Api}注解指定。
 * 方法级别配置存在时会与类级别配置进行合并，绝大多数配置项以方法级别的非空值优先。
 *
 * <p><b>1. 客户端级别专属配置</b>
 * <pre>{@code
 * url                     服务地址，与service二选一（同时配置时url优先）
 * service                 服务名，通过Spring Cloud LoadBalancer从注册中心解析出真实地址
 * lifecycle-manager       生命周期管理器，可指定type/beanName/scope/consumerClass
 * method-configs          方法级个性化配置，key为apiId
 * method-spel-import      MethodContext级别的SpEL导入配置（向上下文中导入变量、函数、Hooks、包）
 * }</pre>
 *
 * <p><b>2. 通用配置（类级别与方法级别均可配置）</b>
 * <pre>{@code
 * ─ 请求基础信息 ─
 * path                        接口路径，类级别与方法级别的path会逐级拼接
 * desc                        接口描述信息
 * method                      请求方法（GET/POST/PUT/DELETE...），默认POST
 * connect-timeout             连接超时时间（毫秒）
 * read-timeout                读取超时时间（毫秒）
 * write-timeout               写入超时时间（毫秒，OkHttp执行器特有）
 * call-timeout                整体超时时间（毫秒，OkHttp执行器特有）
 * connection-request-timeout  获取连接的超时时间（毫秒，HttpClient执行器特有）
 *
 * ─ 请求参数（参数名称与值均支持SpEL，集合值会展开为多个同名参数） ─
 * header-params               请求头参数
 * path-params                 路径参数，用于填充path中的{name}占位符
 * query-params                URL查询参数
 * form-params                 application/x-www-form-urlencoded表单参数
 * multipart-form-params       multipart/form-data参数（支持文件上传）
 * body                        请求体（支持SpEL；可解析为BodyObject/InputStreamSource/File/byte[]/字符串等）
 *
 * ─ 条件请求参数（condition为SpEL条件表达式，条件成立时才使用同组中的配置） ─
 * condition-header-params         条件请求头参数（condition + configs）
 * condition-path-params           条件路径参数（condition + configs）
 * condition-query-params          条件Query参数（condition + configs）
 * condition-form-params           条件表单参数（condition + configs）
 * condition-multipart-form-params 条件Multipart-Form参数（condition + configs）
 * condition-body                  条件请求体（condition + body）
 *
 * ─ 高级参数 ─
 * init-bind-params            初始化绑定参数：bind-classes指定需要绑定的参数类型，
 *                             bind-params为绑定配置（支持SpEL）；当参数值非空且参数上
 *                             标注了@Init或参数类型命中bind-classes时，会将bind-params绑定到该参数对象上
 * additional-params           额外的自定义参数，合并后可通过SpEL变量$StandardApiConfiguration读取
 *
 * ─ 响应转换 ─
 * meta-type                   转化元类型表达式，结果必须为ResolvableType类型
 * response-content-type       强制指定响应体的Content-Type
 * condition-meta-type         条件化的转化元类型（condition + meta-type）
 * condition-resp-content-type 条件化的响应Content-Type（condition + response-content-type）
 * result-convert              响应转换SpEL表达式，例如"#{$body$.data}"表示只取响应体的data部分进行转换
 * condition-convert           条件转换（assertion条件 + result/exception）
 * generate-response-java-bean 用于生成响应对象对应的JavaBean的配置
 *
 * ─ 增强功能 ─
 * retry-config                重试配置（enable/count/waitMillis/condition/exceptionClasses等），
 *                             enable=true时生效
 * ssl-config                  SSL配置（enable/protocol/keyStoreInfo/trustStoreInfo等），
 *                             enable=true时生效
 * mock-config                 Mock配置，见下文
 * cache-config                缓存配置，见下文
 * exception-handler           异常处理配置，见下文
 * condition-exception-handler 条件化异常处理配置，见下文
 * method-result-running       方法结果转换完成后执行的SpEL表达式列表
 * destroy-running             方法上下文销毁时执行的SpEL表达式列表
 * spel-import                 当前上下文级别的SpEL导入配置
 * method-meta-spel-import     MethodMetaContext级别的SpEL导入配置
 * }</pre>
 *
 * <p><b>3. mock-config（Mock配置）</b>
 * <pre>{@code
 * enable      是否启用Mock
 * latency     模拟延时（毫秒）
 * status      模拟HTTP状态码
 * headers     模拟响应头
 * body.txt    模拟文本类型的响应体
 * body.file   模拟文件类型的响应体
 * match       条件化的Mock结果列表（列表项：when条件表达式 + latency/status/headers/body）
 * }</pre>
 * 配置示例：
 * <pre>{@code
 * mock-config:
 *   enable: true
 *   status: 200
 *   headers:
 *     Content-Type: application/json
 *   body:
 *     txt: '{"code":0,"data":"mock数据"}'
 *   match:
 *     - when: "#{#username} == 'admin'"
 *       status: 403
 *       body:
 *         txt: '{"code":403,"message":"forbidden"}'
 * }</pre>
 *
 * <p><b>4. cache-config（缓存配置，详见{@link CacheConfig}）</b>
 * <pre><code>
 * enable                     是否开启缓存，只有值为true时才会启用
 * type                       缓存类型：MEMORY（默认）/ REDIS，详见{@link CacheType}
 * key                        缓存key（支持SpEL），启用缓存时为必填项，未配置会抛出异常
 * expires                    过期时间（毫秒，支持SpEL），小于0表示永不过期，未配置时默认为-1
 * redis-template-bean-name   REDIS类型缓存使用的RedisTemplate的Bean名称
 * memory-capacity            MEMORY类型缓存的最大容量，小于等于0表示不限制，超出容量后按LRU策略淘汰
 * memory-save-dir            MEMORY类型缓存数据的持久化目录，配置后数据会持久化并支持重启后恢复
 * </code></pre>
 * 类级别与方法级别的缓存配置会合并，方法级别的非空配置优先。
 * 配置示例（类级别启用缓存，个别方法单独关闭）：
 * <pre>{@code
 * cache-config:
 *   enable: true
 *   type: memory
 *   key: "gitee:user:#{#username}"
 *   expires: "60000"
 * method-configs:
 *   getUserRepos:
 *     cache-config:
 *       enable: false
 * }</pre>
 *
 * <p><b>5. exception-handler（异常处理配置）</b>
 * <pre>{@code
 * running     异常时需要执行的SpEL表达式列表
 * result      异常时返回的默认结果（支持SpEL）
 * exception   异常时转为抛出的自定义异常（支持SpEL）
 * }</pre>
 *
 * <p><b>6. condition-exception-handler（条件化异常处理配置，列表结构）</b>
 * <pre>{@code
 * condition          条件表达式
 * exception-classes  可以处理的异常类型
 * exception-compare  异常类型比较算法：EQUALS（默认，Class#equals）/ EXTEND（Class#isAssignableFrom）
 * running / result / exception  同exception-handler
 * }</pre>
 * 配置示例：
 * <pre>{@code
 * condition-exception-handler:
 *   - condition: "#{$status$ == 404}"
 *     exception-classes: "com.example.ApiException"
 *     exception-compare: EXTEND
 *     result: "#{null}"
 * }</pre>
 *
 * <h2>四、SpEL支持</h2>
 *
 * <p>配置中的大部分配置项（参数名称与值、超时时间、缓存key与过期时间、Mock开关、条件表达式、
 * 响应转换表达式等）均支持SpEL表达式，使用{@code #{...}}语法。
 * 表达式中可以直接引用方法参数与内置上下文变量，常用的内置变量有：
 * <pre>{@code
 * $mc$        当前方法上下文（MethodContext）
 * $mec$       当前元方法上下文（MethodMetaContext）
 * $req$       当前请求对象
 * $resp$      当前响应对象
 * $body$      对象类型的响应体（$stringBody$/$byteBody$/$streamBody$等为其他形式的响应体）
 * $status$    响应状态码
 * $respHeader$ 响应头
 * $err$       请求过程中出现的异常
 * $StandardApiConfiguration  当前生效的配置对象（可通过其additionalParams读取额外的自定义参数）
 * }</pre>
 * 方法参数的引用方式（以参数名username、第1个参数为例）：
 * <pre>{@code
 * #username      参数最终值
 * #$username     参数原始值
 * #$0 / #$1      按参数位置引用（第1个/第2个参数的最终值）
 * #username::type  参数类型
 * }</pre>
 * 按参数名引用的前提是编译时保留了方法参数名（javac的{@code -parameters}编译参数或IDE中
 * 开启"在字节码中存储方法参数名"选项），否则请使用{@code #$0}形式按位置引用。
 *
 * <h2>五、注意事项</h2>
 * <ul>
 *   <li>url与service必须至少配置一个，否则调用时会抛出ConfigurationParserException；</li>
 *   <li>默认开启了初始化配置校验（{@code lucky.http-client.enable-std-client-init-check=true}），
 *       接口缺少必要配置时会直接抛出异常，可将其设置为false改为仅打印告警日志；</li>
 *   <li>启用缓存时必须配置cache-config.key，否则缓存插件注册时会抛出异常；</li>
 *   <li>未在method-configs中配置的方法将直接使用类级别配置；</li>
 *   <li>接口必须能被{@code @EnableLuckyHttpClient}/{@code @EnableLuckyHttpAutoScan}扫描到；</li>
 *   <li>建议为项目开启{@code -parameters}编译参数，以便在SpEL中按参数名引用方法参数。</li>
 * </ul>
 *
 * @see CacheConfig
 * @see CacheType
 * @see StdCacheAdapter
 * @see LifeCycleManager
 * @see StandardLifeCycleManager
 * @see StandardApiConfiguration
 * @see StandardHttpClientConfiguration
 * @since 3.0.3
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ApiConfig
@HttpRequest
@HttpClient(urlFunc = "__std_http_server_url__", serviceFunc = "__std_http_service_name__")
@Mock(enableFunc = "__std_mock_enable__", mockFunc = "__std_mock_result__")
@CachePluginMeta(enable = "#{__std_cache_enable__($mc$)}", key = "#{__std_cache_key__($mc$)}", expires = "#{__std_cache_expires__($mc$)}", cache = StdCacheAdapter.class)
@RespConvert(metaTypeFunc = "__std_response_meta_type__", resultFunc = "__std_result_convert__", respContentTypeFunc = "__std_response_content_type__")
@ExceptionHandle(conditionFunc = "__std_enable_exception_handler__", handleFunc = "__std_exception_handler__", exceptions = Throwable.class)
@SpELImport({GeneratedResponseJavaBeanFunction.class, StdHttpClientFunction.class})
public @interface StdHttpClient {

    /**
     * 配置ID，配置ID相同的接口共享同一套配置
     */
    @AliasFor(annotation = ApiConfig.class, attribute = "value")
    String configId() default "";

    /**
     * 配置Bean的名称，同{@link Component#value()}
     */
    @AliasFor(annotation = HttpClient.class, attribute = "beanId")
    String beanId() default "";

    /**
     * 支持SpEL表达式
     * path，全局路径前缀，请求时会自动加上
     */
    @AliasFor(annotation = HttpClient.class, attribute = "path")
    String path() default "";

    /**
     * 生命周期管理器对象的 Class
     */
    Class<? extends LifeCycleManager> lifecycle() default StandardLifeCycleManager.class;

    /**
     * 生命周期管理器对象的 Bean 名称
     */
    String lifecycleBean() default "";

    /**
     * 生命周期管理器对象生成器对象
     */
    ObjectGenerate lifecycleGenerate() default @ObjectGenerate(LifeCycleManager.class);
}
