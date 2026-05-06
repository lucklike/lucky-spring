package io.github.lucklike.httpclient.mock;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.ClassContext;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.mock.Mock;
import com.luckyframework.httpclient.proxy.mock.MockResponse;
import com.luckyframework.httpclient.proxy.mock.config.MockConfigFunction;
import com.luckyframework.httpclient.proxy.mock.config.MockConfiguration;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import com.luckyframework.httpclient.proxy.spel.hook.Lifecycle;
import com.luckyframework.httpclient.proxy.spel.hook.callback.Callback;
import io.github.lucklike.httpclient.function.BeanFunction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动识别环境变量中的Mock配置
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/4/30 00:27
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Mock(enable = "#{__config_mock_enable__($mc$)}", mockResp = "#{__config_mock_result__($mc$)}")
@SpELImport(AutoIdentifyMockEnvConfig.MockConfigFunctionAndCallback.class)
public @interface AutoIdentifyMockEnvConfig {

    /**
     * 配置前缀
     */
    String value();


    /**
     * Mock配置相关的工具函数与回调函数
     */
    class MockConfigFunctionAndCallback {

        public static final String MOCK_CONFIG = "$AutoIdentifyMockEnvConfig";

        /**
         * 初始化Mock配置，检查Mock配置是否存在，存在则加载
         *
         * @param cc 类上下文对象
         * @return Mock配置
         */
        @Callback(lifecycle = Lifecycle.CLASS, storeOrNot = true, storeName = MOCK_CONFIG)
        public static MockConfiguration initMockConfiguration(ClassContext cc) {
            AutoIdentifyMockEnvConfig annConfig = cc.getMergedAnnotation(AutoIdentifyMockEnvConfig.class);
            String configKey = cc.parseExpression(annConfig.value(), String.class);
            if (!StringUtils.hasText(configKey)) {
                return null;
            }
            try {
                return BeanFunction.env(configKey, MockConfiguration.class);
            } catch (Exception e) {
                return null;
            }

        }

        /**
         * 是否执行Mock逻辑，Mock配置对象存在且开关开启
         *
         * @param mc 方法上下文
         * @return 是否执行Mock逻辑
         */
        @FunctionAlias("__config_mock_enable__")
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
        @FunctionAlias("__config_mock_result__")
        public static MockResponse mockResult(MethodContext mc) throws InterruptedException {

            // 将Mock配置转化为MockResponse对象
            MockConfiguration mockConfig = mc.getRootVar(MOCK_CONFIG, MockConfiguration.class);
            MockResponse mockResponse = MockConfigFunction.mockResult(mc, mockConfig);

            // 设置特殊Mock响应头
            AutoIdentifyMockEnvConfig mockConfigAnn = mc.getMergedAnnotationCheckParent(AutoIdentifyMockEnvConfig.class);
            mockResponse.header("Mock-Annotation", "@AutoIdentifyMockEnvConfig");
            mockResponse.header("Mock-Environment-Prefix", mockConfigAnn.value());
            mockResponse.header("Mock-Environment-Property", MockConfigFunction.getApiName(mc));

            //return
            return mockResponse;
        }


    }
}
