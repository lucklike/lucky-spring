package io.github.lucklike.httpclient.hook;

import com.luckyframework.httpclient.proxy.spel.hook.HookContext;
import com.luckyframework.httpclient.proxy.spel.hook.NamespaceWrap;
import com.luckyframework.httpclient.proxy.spel.hook.callback.AbstractValueStoreHookHandler;

public class EnvImportHookHandler extends AbstractValueStoreHookHandler {

    @Override
    protected Object useHookReturnResult(HookContext context, NamespaceWrap namespaceWrap) {
        return null;
    }

    @Override
    protected String geDefStoreName(NamespaceWrap namespaceWrap) {
        return "$";
    }

    @Override
    protected String getStoreDesc(NamespaceWrap namespaceWrap) {
        return "";
    }

}
