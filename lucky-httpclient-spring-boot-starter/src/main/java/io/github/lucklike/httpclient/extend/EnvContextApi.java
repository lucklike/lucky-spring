package io.github.lucklike.httpclient.extend;

import com.luckyframework.httpclient.proxy.context.MethodContext;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2024/7/1 01:22
 */
public class EnvContextApi {
    private final EnvApi api;
    private final MethodContext context;

    public EnvContextApi(EnvApi api, MethodContext context) {
        this.api = api;
        this.context = context;
    }

    public EnvApi getApi() {
        return api;
    }

    public MethodContext getContext() {
        return context;
    }
}
