package io.github.lucklike.httpclient.mock;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.common.Resources;
import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.mock.Mock;
import com.luckyframework.httpclient.proxy.mock.MockResponse;
import com.luckyframework.httpclient.proxy.spel.FunctionAlias;
import com.luckyframework.httpclient.proxy.spel.SpELImport;
import io.github.lucklike.httpclient.ApplicationContextUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

/**
 * 使用 Mock 配置
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
@SpELImport(UseMockConfiguration.MockConfigFunction.class)
public @interface UseMockConfiguration {

    String DEF_MOCK_BEAN_SUFFIX = "$MockConfiguration";

    String configBean() default "";

    class MockConfigFunction {

        @FunctionAlias("__config_mock_enable__")
        public static boolean mockEnable(MethodContext mc) {
            UseMockConfiguration mockConfigAnn = mc.getMergedAnnotationCheckParent(UseMockConfiguration.class);
            String beanName = getMockConfigurationBeanName(mc, mockConfigAnn.configBean());

            // 不存在该Bean定义信息时
            if (!ApplicationContextUtils.containsBeanDefinition(beanName)) {
                return false;
            }

            // 判断总开关
            MockConfiguration mockConfig = ApplicationContextUtils.getBean(beanName, MockConfiguration.class);
            if (!mockConfig.isEnable()) {
                return false;
            }

            // 判断方法级别开关
            Map<String, MockResult> methods = mockConfig.getMethods();
            String methodName = mc.getCurrentAnnotatedElement().getName();
            MockResult methodMock = methods.get(methodName);
            return methodMock != null && methodMock.isEnable();
        }


        @FunctionAlias("__config_mock_result__")
        public static MockResponse mockResult(MethodContext mc) throws InterruptedException {
            UseMockConfiguration mockConfigAnn = mc.getMergedAnnotationCheckParent(UseMockConfiguration.class);
            String beanName = getMockConfigurationBeanName(mc, mockConfigAnn.configBean());

            MockResponse mockResponse = MockResponse.create();
            mockResponse.header("Mock-Annotation", "@UseMockConfiguration");
            mockResponse.header("Mock-Config-Bean", beanName);

            MockConfiguration mockConfig = ApplicationContextUtils.getBean(beanName, MockConfiguration.class);
            String methodName = mc.getCurrentAnnotatedElement().getName();
            MockResult mockResult = mockConfig.getMethods().get(methodName);

            // main
            Long latency = mockResult.getLatency() == null ? mockConfig.getLatency() : mockResult.getLatency();
            Integer status = mockResult.getStatus();
            Map<String, Object> headers = mockResult.getHeaders();

            // match
            boolean bodySetter = false;
            List<WhenMockResult> matchList = mockResult.getMatch();
            if (ContainerUtils.isNotEmptyCollection(matchList)) {
                for (WhenMockResult math : matchList) {

                    // 判断when表达式是否成立
                    String whenExp = math.getWhen();
                    if (!StringUtils.hasText(whenExp) || !mc.parseExpression(whenExp, boolean.class)) {
                        continue;
                    }

                    mockResponse.header("Mock-Branch", whenExp);

                    // latency
                    latency = math.getLatency() == null ? latency : math.getLatency();

                    // status
                    status = math.getStatus() == null ? status : math.getStatus();

                    // headers
                    Map<String, Object> _headers = math.getHeaders();
                    if (ContainerUtils.isNotEmptyMap(headers)) {
                        if (ContainerUtils.isNotEmptyMap(_headers)) {
                            headers.putAll(_headers);
                        }
                    } else {
                        headers = _headers;
                    }

                    // body
                    MockBody body = math.getBody();
                    // file
                    if (StringUtils.hasText(body.getFile())) {
                        mockResponse.resource(Resources.getResource(mc.parseExpression(body.getFile(), String.class)));
                        bodySetter = true;
                    }

                    // txt
                    if (!bodySetter) {
                        if (StringUtils.hasText(body.getTxt())) {
                            mockResponse.body(mc.parseExpression(body.getTxt(), String.class));
                            bodySetter = true;
                        }
                    }
                    break;
                }
            }

            // status
            setStatus(mockResponse, status);

            // header
            setHeaders(mc, mockResponse, headers);

            // body
            MockBody body = mockResult.getBody();
            if (!bodySetter) {
                if (StringUtils.hasText(body.getFile())) {
                    mockResponse.resource(Resources.getResource(mc.parseExpression(body.getFile(), String.class)));
                    bodySetter = true;
                }

                // TXT
                if (!bodySetter) {
                    if (StringUtils.hasText(body.getTxt())) {
                        mockResponse.body(mc.parseExpression(body.getTxt(), String.class));
                    }
                }
            }

            //latency
            setLatency(latency);

            //return
            return mockResponse;
        }

        private static String getMockConfigurationBeanName(MethodContext mc, String configBean) {
            if (StringUtils.hasText(configBean)) {
                return mc.parseExpression(configBean, String.class);
            }
            String[] beanNamesForType = ApplicationContextUtils.getBeanNamesForType(mc.getClassContext().getCurrentAnnotatedElement());
            return beanNamesForType[0] + DEF_MOCK_BEAN_SUFFIX;
        }


        /**
         * 设置状态
         *
         * @param mockResponse Mock响应
         * @param status       状态配置
         */
        private static void setStatus(MockResponse mockResponse, Integer status) {
            mockResponse.status(status == null ? 200 : status);
        }

        /**
         * 设置延时
         *
         * @param latency 延时配置
         * @throws InterruptedException 可能出现的异常
         */
        private static void setLatency(Long latency) throws InterruptedException {
            if (latency != null && latency > 0) {
                Thread.sleep(latency);
            }
        }

        /**
         * 设置响应头
         *
         * @param mc           方法上下文
         * @param mockResponse Mock响应
         * @param headers      响应头配置
         */
        private static void setHeaders(MethodContext mc, MockResponse mockResponse, Map<String, Object> headers) {
            if (ContainerUtils.isNotEmptyMap(headers)) {
                headers.forEach((k, v) -> {
                    String hName = mc.parseExpression(k, String.class);
                    if (ContainerUtils.isIterable(v)) {
                        ContainerUtils.getIterable(v).forEach(e -> {
                            mockResponse.header(hName, mc.parseExpression(String.valueOf(e)));
                        });
                    } else {
                        mockResponse.header(hName, mc.parseExpression(String.valueOf(v)));
                    }
                });
            }
        }

    }
}
