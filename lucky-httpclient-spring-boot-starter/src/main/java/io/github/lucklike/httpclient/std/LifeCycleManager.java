package io.github.lucklike.httpclient.std;

import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.proxy.context.MethodContext;
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
     * 构建基本 Url
     *
     * @param mc        方法上下文对象
     * @param apiConfig 配置信息
     * @return 基本 URL
     */
    String buildBaseUrl(MethodContext mc, StandardApiConfiguration apiConfig) throws Exception;

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
    default void requestCompleted(MethodContext mc, Request request, StandardApiConfiguration apiConfig) throws Exception {

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
