package io.github.lucklike.httpclient.injection.parameter;

import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpack;
import com.luckyframework.httpclient.proxy.unpack.ContextValueUnpackException;
import com.luckyframework.httpclient.proxy.unpack.ValueUnpackContext;
import io.github.lucklike.httpclient.injection.Bind;

public class BindContentValueUnpack implements ContextValueUnpack {

    @Override
    public Object getRealValue(ValueUnpackContext unpackContext, Object wrapperValue) throws ContextValueUnpackException {
        Bind bindAnn = unpackContext.getMergedAnnotation(Bind.class);
        if (bindAnn != null) {

        }
        return wrapperValue;
    }

}
