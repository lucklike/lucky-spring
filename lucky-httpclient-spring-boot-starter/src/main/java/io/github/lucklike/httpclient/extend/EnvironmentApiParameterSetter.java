package io.github.lucklike.httpclient.extend;

import com.luckyframework.common.StringUtils;
import com.luckyframework.conversion.ConversionUtils;
import com.luckyframework.httpclient.core.executor.HttpExecutor;
import com.luckyframework.httpclient.core.meta.BodyObject;
import com.luckyframework.httpclient.core.meta.Request;
import com.luckyframework.httpclient.proxy.context.MethodContext;
import com.luckyframework.httpclient.proxy.paraminfo.ParamInfo;
import com.luckyframework.httpclient.proxy.setter.ParameterSetter;
import com.luckyframework.httpclient.proxy.setter.UrlParameterSetter;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spring环境变量API参数设置器
 *
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

        if (StringUtils.hasText(api.getUrl())) {
            urlSetter.doSet(request, "url", context.parseExpression(api.getUrl(), String.class));
        }

        if (api.getMethod() != null) {
            request.setRequestMethod(api.getMethod());
        }

        if (api.getConnectTimeout() != null) {
            request.setConnectTimeout(context.parseExpression(api.getConnectTimeout(), Integer.class));
        }

        if (api.getReadTimeout() != null) {
            request.setReadTimeout(context.parseExpression(api.getReadTimeout(), Integer.class));
        }

        if (api.getWriteTimeout() != null) {
            request.setWriterTimeout(context.parseExpression(api.getWriteTimeout(), Integer.class));
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
            String[] resStrArray = ConversionUtils.conversion(v, String[].class);
            List<Resource> resourceList = new ArrayList<>();
            for (String resStr : resStrArray) {
                resStr = context.parseExpression(resStr, String.class);
                resourceList.addAll(Arrays.asList(ConversionUtils.conversion(resStr, Resource[].class)));
            }
            request.addHttpFiles(key, HttpExecutor.toHttpFiles(resourceList));
        });

        Body body = api.getBody();

        // JSON
        if (StringUtils.hasText(body.getJson())) {
            String jsonBody = context.parseExpression(body.getJson(), String.class);
            request.setBody(BodyObject.jsonBody(jsonBody));
        }
        // XML
        else if (StringUtils.hasText(body.getXml())) {
            String xmlBody = context.parseExpression(body.getXml(), String.class);
            request.setBody(BodyObject.xmlBody(xmlBody));
        }
        // FORM
        else if (StringUtils.hasText(body.getForm())) {
            String formBody = context.parseExpression(body.getForm(), String.class);
            String charset = context.parseExpression(body.getCharset(), String.class);
            request.setBody(BodyObject.builder("application/x-www-form-urlencoded", charset, formBody));
        }
        // 二进制格式
        else if (body.getFile() != null) {
            try {
                Resource resource = context.parseExpression(body.getFile(), Resource.class);
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                request.setBody(BodyObject.builder("application/octet-stream", (Charset) null, bytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // 自定义格式
        else if (body.getData() != null) {
            String mimeType = context.parseExpression(body.getMimeType(), String.class);
            String charset = context.parseExpression(body.getCharset(), String.class);
            String data = context.parseExpression(body.getData(), String.class);
            request.setBody(BodyObject.builder(mimeType, charset, data));
        }
    }

}
