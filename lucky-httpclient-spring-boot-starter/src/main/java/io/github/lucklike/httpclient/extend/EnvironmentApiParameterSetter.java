package io.github.lucklike.httpclient.extend;

import com.luckyframework.common.StringUtils;
import com.luckyframework.httpclient.core.meta.BodyObject;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.core.meta.RequestMethod;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.paraminfo.ParamInfo;
import com.luckyframework.httpclient.proxy.setter.ParameterSetter;
import com.luckyframework.httpclient.proxy.setter.UrlParameterSetter;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2024/6/30 16:24
 */
public class EnvironmentApiParameterSetter implements ParameterSetter {

    private final UrlParameterSetter urlSetter = new UrlParameterSetter();

    @Override
    public void set(Request request, ParamInfo paramInfo) {
        EnvContextApi contextApi = (EnvContextApi) paramInfo.getValue();
        EnvApi api = contextApi.getApi();
        MethodContext context = contextApi.getContext();

        String url = api.getUrl();
        RequestMethod method = api.getMethod();


        if (StringUtils.hasText(url)) {
            urlSetter.doSet(request, "url", context.parseExpression(url, String.class));
        }
        if (method != null) {
            request.setRequestMethod(method);
        }
        api.getHeader().forEach((k, v) -> {
            String key = context.parseExpression(k, String.class);
            Object value = context.parseExpression(String.valueOf(v));
            request.setHeader(key, value);
        });

        api.getQuery().forEach((k, v) -> {
            String key = context.parseExpression(k, String.class);
            v.forEach(e -> {
                Object value = context.parseExpression(String.valueOf(e));
                request.addQueryParameter(key, value);
            });
        });

        api.getPath().forEach((k, v) -> {
            String key = context.parseExpression(k, String.class);
            Object value = context.parseExpression(String.valueOf(v));
            request.addPathParameter(key, value);
        });

        api.getForm().forEach((k, v) -> {
            String key = context.parseExpression(k, String.class);
            Object value = context.parseExpression(String.valueOf(v));
            request.addFormParameter(key, value);
        });

        api.getMultiData().forEach((k, v) -> {
            String key = context.parseExpression(k, String.class);
            Object value = context.parseExpression(String.valueOf(v));
            request.addMultipartFormParameter(key, value);
        });

        api.getMultiFile().forEach((k, v) -> {
            String key = context.parseExpression(k, String.class);
            Resource value = context.parseExpression(String.valueOf(v), Resource.class);
            request.addResources(key, value);
        });

        Body body = api.getBody();
        if (body.getData() != null) {
            String mimeType = context.parseExpression(body.getMimeType(), String.class);
            String charset = context.parseExpression(body.getCharset(), String.class);
            String data = context.parseExpression(body.getData(), String.class);
            request.setBody(BodyObject.builder(mimeType, charset, data));
        }
        else if (body.getFile() != null) {
            try {
                String mimeType = context.parseExpression(body.getMimeType(), String.class);
                String charset = context.parseExpression(body.getCharset(), String.class);
                Resource resource = context.parseExpression(body.getFile(), Resource.class);
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                request.setBody(BodyObject.builder(mimeType, charset, bytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
