package io.github.lucklike.httpclient.mock;

import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.proxy.context.ClassContext;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import com.luckyframework.httpclient.proxy.mock.Mock;
import com.luckyframework.httpclient.proxy.mock.MockResponse;
import com.luckyframework.httpclient.proxy.mock.config.MockConfigFunction;
import com.luckyframework.httpclient.proxy.mock.config.MockConfiguration;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import com.luckyframework.httpclient.proxy.spel.hook.Lifecycle;
import com.luckyframework.httpclient.proxy.spel.hook.callback.Callback;
import io.github.lucklike.httpclient.config.HttpClientProxyObjectFactoryConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import static io.github.lucklike.httpclient.Constant.PROXY_FACTORY_CONFIG_BEAN_NAME;

/**
 * 自动识别环境变量中的Mock配置，默认的配置前缀为${lucky.http-client.mock-configs.#{$class$.getSimpleName()}}
 *
 * <pre>
 *  eg:
 *  Class Api:
 * </pre>
 * <pre>
 *  {@code
 *      @HttpClient("http://localhost:8080")
 *      @AutoIdentifyFileMock
 *      public interface AutoIdentifyFileMockApi {
 *
 *          @Get("login")
 *          SpelBean<?> login();
 *
 *          @Post("logout")
 *          String logout(@JsonParam String token);
 *      }
 *  }
 * </pre>
 *
 * <pre>
 *   Mock_AutoIdentifyFileMockApi.yml:
 * </pre>
 *
 * <pre>
 *  {@code
 *  #总开关
 * enable: true
 * #方法级别的延时模拟，（单位：毫秒）
 * latency: 1000
 *
 * #各个方法的Mock配置
 * method-configs:
 *     #login方法的Mock数据
 *     login:
 *         #方法级别开关
 *         enable: false
 *         #条件匹配
 *         match:
 *           #条件1+结果1
 *           - when: "#{1==1}"
 *             latency: 1200
 *             status: 200
 *             headers:
 *               Server: nginx/1.18.0
 *               Date: Mon, 16 Mar 2026 06:46:14 GMT
 *               Content-Length: 157
 *             body:
 *               txt: qwqw
 *               file: classpath:deded/lll.txt
 *           # 条件2+结果2
 *           - when: "#{c == 3}"
 *             latency: 1300
 *             status: 404
 *             headers:
 *               Server: nginx/1.18.0
 *               Date: Mon, 16 Mar 2026 06:46:14 GMT
 *               Content-Length: 157
 *             body:
 *               txt: 404 Not Found
 *               file: classpath:deded/lll.txt
 *
 *         #延时模拟，（单位：毫秒）
 *         latency: 1000
 *         #状态码
 *         status: 200
 *         #响应头，Key和Value均支持SpEL表达式
 *         headers:
 *           Content-Type: application/json
 *           X-Random-Emial: #{random_email()};
 *         #响应体
 *         body:
 *           #文本格式响应体，支持SpEL表达式
 *           txt: |
 *             {
 *               "access_token": "e6c0991176784141583030b2af550655812729af8cd92598b5b99a9c0f89",
 *               "expire_time": 36000,
 *               "expires_in": "2026-01-08 21:06:04",
 *               "random_tel": "#{random_tel()}"
 *             }
 *           #文件类型的响应体
 *           file: classpath:test/mocak.pdf
 *
 *     #logout方法的Mock数据
 *     logout:
 *         headers:
 *           Content-Type: application/json
 *         body:
 *           txt: |
 *             {
 *                 "error": {
 *                     "error_no": "0",
 *                     "error_info": "",
 *                     "error_pathinfo": null
 *                 },
 *                 "data": {
 *                     "staff_no": "1163",
 *                     "user_id": "1163",
 *                     "user_name": "fukang7075",
 *                     "info": "【#{$0}】登出成功"
 *                 }
 *             }
 *  }
 * </pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/7 01:47
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Mock(enable = "#{__def_config_mock_enable__($mc$)}", mockResp = "#{__def_config_mock_result__($mc$)}")
@SpELImport(AutoIdentifyDefaultMockConfiguration.MockConfigFunctionAndCallback.class)
public @interface AutoIdentifyDefaultMockConfiguration {

    /**
     * Mock配置相关的工具函数与回调函数
     */
    class MockConfigFunctionAndCallback {

        public static final String MOCK_CONFIG = "$AutoIdentifyDefaultMockConfiguration";

        /**
         * 初始化Mock配置，检查Mock配置是否存在，存在则加载
         *
         * @param cc 类上下文对象
         * @return Mock配置
         */
        @Callback(lifecycle = Lifecycle.CLASS, storeOrNot = true, storeName = MOCK_CONFIG)
        public static MockConfiguration loadMockConfiguration(ClassContext cc,
                                                              @Qualifier(PROXY_FACTORY_CONFIG_BEAN_NAME) HttpClientProxyObjectFactoryConfiguration factoryConfiguration) {
            Map<String, io.github.lucklike.httpclient.config.mock.MockConfiguration> mockConfigs = factoryConfiguration.getMockConfigs();
            io.github.lucklike.httpclient.config.mock.MockConfiguration mockConfiguration = mockConfigs.get(CommonFunctions.getApiConfigId(cc));
            return ConversionUtils.conversion(mockConfiguration, MockConfiguration.class);
        }


        /**
         * 是否执行Mock逻辑，Mock配置对象存在且开关开启
         *
         * @param mc 方法上下文
         * @return 是否执行Mock逻辑
         */
        @FunctionAlias("__def_config_mock_enable__")
        public static boolean mockEnable(MethodContext mc) {
            MockConfiguration mockConfig = mc.getRootVar(MOCK_CONFIG, MockConfiguration.class);
            return MockConfigFunction.mockEnable(mc, mockConfig);
        }


        /**
         * 将Mock配置对象转化为{@link MockResponse}对象
         *
         * @param mc 方法上下文
         * @return {@link MockResponse}对象
         * @throws InterruptedException 可能出现的异常
         */
        @FunctionAlias("__def_config_mock_result__")
        public static MockResponse mockResult(MethodContext mc) throws InterruptedException {

            // 将Mock配置转化为MockResponse对象
            MockConfiguration mockConfig = mc.getRootVar(MOCK_CONFIG, MockConfiguration.class);
            MockResponse mockResponse = MockConfigFunction.mockResult(mc, mockConfig);

            // 设置特殊Mock响应头
            mockResponse.header("Mock-Annotation", "@AutoIdentifyDefaultMockConfiguration");
            mockResponse.header("Mock-Environment-Prefix", "lucky.http-client.mock-configs." + CommonFunctions.getApiConfigId(mc.getClassContext()));
            mockResponse.header("Mock-Environment-Property", CommonFunctions.getApiId(mc));

            //return
            return mockResponse;
        }

    }
}
