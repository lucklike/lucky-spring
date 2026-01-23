package io.github.lucklike.httpclient.function;

import com.luckyframework.common.ContainerUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.executor.JdkHttpExecutor;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.RequestMethod;
import com.luckyframework.httpclient.proxy.HttpClientProxyObjectFactory;
import com.luckyframework.httpclient.proxy.function.CommonFunctions;
import com.luckyframework.httpclient.proxy.spel.FunctionFilter;
import com.luckyframework.httpclient.proxy.spel.Namespace;
import com.luckyframework.serializable.SerializationTypeToken;
import io.github.lucklike.httpclient.ApplicationContextUtils;
import org.springframework.core.ResolvableType;

import java.util.Map;

import static io.github.lucklike.httpclient.Constant.SIMPLE_HTTP_EXECUTOR;

/**
 * 提供简单Http执行器相关的功能
 */
@Namespace(SIMPLE_HTTP_EXECUTOR)
public class SimpleHttpExecutorFunction {

    /**
     * 负责执行HTTP请求的执行器
     */
    private static HttpExecutor httpExecutor;

    /**
     * 执行一个简单的HTTP请求
     *
     * @param method 请求方法
     * @param url    URL地址
     * @param type   结果转换类型
     * @param <T>    响应结果泛型
     * @return 响应结果
     */
    public static <T> T http(String method, String url, Object... type) {
        return getHttpExecutor().execute(Request.builder(url, RequestMethod.valueOf(method.toUpperCase()))).getEntity(getEntityType(type).getType());
    }

    /**
     * 执行一个简单的HTTP请求，请求体格式为JSON
     *
     * @param method   请求方法
     * @param url      URL地址
     * @param headers  HTTP头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json(String method, String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        // 构建请求
        Request request = Request.builder(url, RequestMethod.valueOf(method.toUpperCase()));

        // 请求体
        if (jsonBody instanceof String) {
            request.setJsonBody((String) jsonBody);
        } else if (jsonBody != null) {
            request.setJsonBody(jsonBody);
        }

        // 请求头
        if (ContainerUtils.isNotEmptyMap(headers)) {
            headers.forEach(request::addHeader);
        }

        return getHttpExecutor().execute(request).getEntity(getEntityType(type).getType());
    }

    /**
     * 执行一个简单的HTTP请求，请求体格式为JSON
     *
     * @param method   请求方法
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json(String method, String url, Object jsonBody, Object... type) {
        return http_header_json(method, url, null, jsonBody, type);
    }


    /**
     * 执行一个简单的HTTP请求，请求体格式为FORM
     *
     * @param method   请求方法
     * @param url      URL地址
     * @param headers  HTTP头信息
     * @param formBody FORM请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T http_header_form(String method, String url, Map<String, Object> headers, Object formBody, Object... type) {
        // 构建请求
        Request request = Request.builder(url, RequestMethod.valueOf(method.toUpperCase()));

        // 构建请求体
        if (formBody instanceof Map) {
            request.setFormParameter((Map<String, Object>) formBody);
        } else if (formBody != null) {
            request.setFormParameter(ConversionUtils.conversion(formBody, new SerializationTypeToken<Map<String, Object>>() {
            }));
        }

        // 请求头
        if (ContainerUtils.isNotEmptyMap(headers)) {
            headers.forEach(request::addHeader);
        }
        return getHttpExecutor().execute(request).getEntity(getEntityType(type).getType());
    }

    /**
     * 执行一个简单的HTTP请求，请求体格式为FORM
     *
     * @param method   请求方法
     * @param url      URL地址
     * @param formBody FORM请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form(String method, String url, Object formBody, Object... type) {
        return http_header_form(method, url, null, formBody, type);
    }

    /**
     * 发起一个简单[GET]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_get(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("GET", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[GET]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_get(String url, Object jsonBody, Object... type) {
        return http_header_json_get(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[GET]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_get(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("GET", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[GET]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_get(String url, Object formBody, Object... type) {
        return http_header_form_get(url, null, formBody, type);
    }


    /**
     * 发起一个简单[GET]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_get(String url, Object... type) {
        return http("GET", url, type);
    }


    /**
     * 发起一个简单[POST]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_post(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("POST", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[POST]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_post(String url, Object jsonBody, Object... type) {
        return http_header_json_post(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[POST]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_post(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("POST", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[POST]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_post(String url, Object formBody, Object... type) {
        return http_header_form_post(url, null, formBody, type);
    }

    /**
     * 发起一个简单[POST]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_post(String url, Object... type) {
        return http("POST", url, type);
    }

    /**
     * 发起一个简单[PUT]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_put(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("PUT", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[PUT]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_put(String url, Object jsonBody, Object... type) {
        return http_header_json_put(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[PUT]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_put(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("PUT", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[PUT]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_put(String url, Object formBody, Object... type) {
        return http_header_form_put(url, null, formBody, type);
    }

    /**
     * 发起一个简单[PUT]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_put(String url, Object... type) {
        return http("PUT", url, type);
    }

    /**
     * 发起一个简单[DELETE]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_delete(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("DELETE", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[DELETE]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_delete(String url, Object jsonBody, Object... type) {
        return http_header_json_delete(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[DELETE]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_delete(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("DELETE", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[DELETE]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_delete(String url, Object formBody, Object... type) {
        return http_header_form_delete(url, null, formBody, type);
    }

    /**
     * 发起一个简单[DELETE]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_delete(String url, Object... type) {
        return http("DELETE", url, type);
    }

    /**
     * 发起一个简单[HEAD]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_head(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("HEAD", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[HEAD]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_head(String url, Object jsonBody, Object... type) {
        return http_header_json_head(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[HEAD]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_head(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("HEAD", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[HEAD]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_head(String url, Object formBody, Object... type) {
        return http_header_form_head(url, null, formBody, type);
    }

    /**
     * 发起一个简单[HEAD]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_head(String url, Object... type) {
        return http("HEAD", url, type);
    }


    /**
     * 发起一个简单[OPTIONS]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_options(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("OPTIONS", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[OPTIONS]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_options(String url, Object jsonBody, Object... type) {
        return http_header_json_options(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[OPTIONS]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_options(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("OPTIONS", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[OPTIONS]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_options(String url, Object formBody, Object... type) {
        return http_header_form_options(url, null, formBody, type);
    }

    /**
     * 发起一个简单[OPTIONS]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_options(String url, Object... type) {
        return http("OPTIONS", url, type);
    }


    /**
     * 发起一个简单[PATCH]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_patch(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("PATCH", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[PATCH]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_patch(String url, Object jsonBody, Object... type) {
        return http_header_json_patch(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[PATCH]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_patch(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("PATCH", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[PATCH]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_patch(String url, Object formBody, Object... type) {
        return http_header_form_patch(url, null, formBody, type);
    }

    /**
     * 发起一个简单[PATCH]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_patch(String url, Object... type) {
        return http("PATCH", url, type);
    }


    /**
     * 发起一个简单[TRACE]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_trace(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("TRACE", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[PATCH]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_trace(String url, Object jsonBody, Object... type) {
        return http_header_json_trace(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[TRACE]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_trace(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("TRACE", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[TRACE]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_trace(String url, Object formBody, Object... type) {
        return http_header_form_trace(url, null, formBody, type);
    }

    /**
     * 发起一个简单[TRACE]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_trace(String url, Object... type) {
        return http("TRACE", url, type);
    }

    /**
     * 发起一个简单[CONNECT]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_json_connect(String url, Map<String, Object> headers, Object jsonBody, Object... type) {
        return http_header_json("CONNECT", url, headers, jsonBody, type);
    }


    /**
     * 发起一个简单[CONNECT]请求，JSON格式请求体
     *
     * @param url      URL地址
     * @param jsonBody JSON请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_json_connect(String url, Object jsonBody, Object... type) {
        return http_header_json_connect(url, null, jsonBody, type);
    }

    /**
     * 发起一个简单[CONNECT]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param headers  请求头信息
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_header_form_connect(String url, Map<String, Object> headers, Object formBody, Object... type) {
        return http_form("CONNECT", url, headers, formBody, type);
    }

    /**
     * 发起一个简单[CONNECT]请求，Form格式请求体
     *
     * @param url      URL地址
     * @param formBody Form请求体
     * @param type     结果转换类型
     * @param <T>      响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_form_connect(String url, Object formBody, Object... type) {
        return http_header_form_connect(url, null, formBody, type);
    }

    /**
     * 发起一个简单[CONNECT]请求
     *
     * @param url  URL地址
     * @param type 结果转换类型
     * @param <T>  响应结果泛型
     * @return 响应结果
     */
    public static <T> T http_connect(String url, Object... type) {
        return http("CONNECT", url, type);
    }


    @FunctionFilter
    private static ResolvableType getEntityType(Object[] type) {
        if (ContainerUtils.isEmptyArray(type)) {
            return ResolvableType.forClass(Object.class);
        }
        return CommonFunctions.toResolvableType(type[0]);
    }

    /**
     * 获取Http执行器，优先使用Spring容器中的，没有则使用默认的{@link JdkHttpExecutor}
     *
     * @return Http执行器
     */
    @FunctionFilter
    private synchronized static HttpExecutor getHttpExecutor() {
        if (httpExecutor == null) {
            HttpClientProxyObjectFactory factory = ApplicationContextUtils.getBeanProvider(HttpClientProxyObjectFactory.class).stream().findFirst().orElse(null);
            if (factory != null) {
                httpExecutor = factory.getHttpExecutor();
            } else {
                httpExecutor = new JdkHttpExecutor();
            }
        }
        return httpExecutor;
    }

}
