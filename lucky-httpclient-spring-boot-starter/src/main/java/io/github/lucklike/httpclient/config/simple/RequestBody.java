package io.github.lucklike.httpclient.config.simple;

import java.util.LinkedHashMap;
import java.util.Map;

public class RequestBody extends SimpleBody {

    /**
     * 某个
     */
    private Map<String, SimpleBody> api = new LinkedHashMap<>();

}
