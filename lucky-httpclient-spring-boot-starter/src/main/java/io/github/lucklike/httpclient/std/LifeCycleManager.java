package io.github.lucklike.httpclient.std;

import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.context.MethodMetaContext;
import com.luckyframework.httpclient.proxy.convert.ActivelyThrownException;
import org.springframework.core.ResolvableType;

import static com.luckyframework.httpclient.proxy.spel.InternalRootVarName.$_RESPONSE_BODY_$;

/**
 * 生命周期管理器
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/7 02:32
 */
public interface LifeCycleManager {

    /**
     * 方法元信息上下文初始化成功时执行
     *
     * @param mec    方法元信息上下文初始化完成时执行
     * @param config 配置信息
     */
    default void methodMetaContentInit(MethodMetaContext mec, StandardApiConfiguration config) {

    }

    /**
     * 方法信息上下文初始化成功时执行
     *
     * @param mc     方法信息上下文初始化完成时执行
     * @param config 配置信息
     */
    default void methodContentInit(MethodContext mc, StandardApiConfiguration config) {

    }

    /**
     * 构建基本 Url
     *
     * @param mc     方法上下文对象
     * @param config 配置信息
     * @return 基本 URL
     */
    String buildBaseUrl(MethodContext mc, StandardHttpClientConfiguration config) throws Exception;

    /**
     * 请求对象刚完成初始化时执行
     *
     * @param mc        方法上下文对象
     * @param request   请求对象
     * @param apiConfig 配置信息
     */
    default void requestInit(MethodContext mc, Request request, StandardApiConfiguration apiConfig) throws Exception {

    }

    /**
     * 请求对象封装完成时调用
     *
     * @param mc        方法上下文对象
     * @param request   请求对象
     * @param apiConfig 配置信息
     */
    default void requestInitCompleted(MethodContext mc, Request request, StandardApiConfiguration apiConfig) throws Exception {

    }

    /**
     * 获取响应元类型
     *
     * @param mc        方法上下文
     * @param apiConfig 配置信息
     * @return 响应元类型
     */
    default ResolvableType getResponseMetaType(MethodContext mc, StandardApiConfiguration apiConfig) throws Exception {
        return ResolvableType.forClass(Object.class);
    }

    /**
     * 强制指定响应体的的Content-Type
     *
     * @param mc        方法上下文
     * @param apiConfig 配置信息
     * @return 响应体的的Content-Type
     */
    default String mandatoryDesignationResponseContentType(MethodContext mc, StandardApiConfiguration apiConfig) {
        return "";
    }

    /**
     * 响应对象返回完成时调用
     *
     * @param mc        方法上下文对象
     * @param response  响应对象
     * @param apiConfig 配置信息
     */
    default void responseCompleted(MethodContext mc, Response response, StandardApiConfiguration apiConfig) throws Exception {

    }

    /**
     * 结果转换，将响应对象转成最终代理方法的返回值
     *
     * @param mc        方法上下文对象
     * @param response  响应对象
     * @param apiConfig 配置信息
     * @return 转化后的最终结果
     */
    default Object resultConvert(MethodContext mc, Response response, StandardApiConfiguration apiConfig) throws Exception {
        return response.getEntity(mc.getResultType());
    }

    /**
     * 方法返回结果转化成功之后调用
     *
     * @param mc        方法上下文对象
     * @param result    方法返回结果
     * @param apiConfig 配置信息
     */
    default void methodResult(MethodContext mc, Object result, StandardApiConfiguration apiConfig) throws Exception {

    }

    /**
     * 销毁上下文时调用
     *
     * @param mc        方法上下文对象
     * @param apiConfig 配置信息
     */
    default void destroy(MethodContext mc, StandardApiConfiguration apiConfig) throws Exception {

    }

    /**
     * 异常处理
     *
     * @param mc        方法上下文
     * @param request   请求对象
     * @param th        异常
     * @param apiConfig 配置信息
     * @return 异常后返回的结果
     */
    Object exceptionHandler(MethodContext mc, Request request, Throwable th, StandardApiConfiguration apiConfig) throws Throwable;

    /**
     * 获取 Body 对象
     *
     * @param mc  方法上下文对象
     * @param <T> Body 对象对应的类型
     * @return Body 对象
     */
    @SuppressWarnings("unchecked")
    default <T> T getBody(MethodContext mc) {
        return (T) mc.getRootVar($_RESPONSE_BODY_$);
    }

}
