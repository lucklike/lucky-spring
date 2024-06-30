package io.github.lucklike.httpclient.extend;

import com.luckyframework.conversion.TargetField;
import com.luckyframework.httpclient.core.meta.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2024/6/30 16:05
 */
public class Api {
    /**
     * 描述信息
     */
    private String desc;

    private String url;

    private RequestMethod method;

    private Map<String, Object> header = new ConcurrentHashMap<>();

    private Map<String, List<Object>> query = new ConcurrentHashMap<>();

    private Map<String, Object> form = new ConcurrentHashMap<>();

    private Map<String, Object> path = new ConcurrentHashMap<>();

    @TargetField("multi-data")
    private Map<String, Object> multiData = new ConcurrentHashMap<>();

    @TargetField("multi-file")
    private Map<String, Object> multiFile = new ConcurrentHashMap<>();

    private Body body = new Body();

    @TargetField("response-convert")
    private Convert responseConvert = new Convert();

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public void setHeader(Map<String, Object> header) {
        this.header = header;
    }

    public Map<String, List<Object>> getQuery() {
        return query;
    }

    public void setQuery(Map<String, List<Object>> query) {
        this.query = query;
    }

    public Map<String, Object> getForm() {
        return form;
    }

    public void setForm(Map<String, Object> form) {
        this.form = form;
    }

    public Map<String, Object> getPath() {
        return path;
    }

    public void setPath(Map<String, Object> path) {
        this.path = path;
    }

    public Map<String, Object> getMultiData() {
        return multiData;
    }

    public void setMultiData(Map<String, Object> multiData) {
        this.multiData = multiData;
    }

    public Map<String, Object> getMultiFile() {
        return multiFile;
    }

    public void setMultiFile(Map<String, Object> multiFile) {
        this.multiFile = multiFile;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Convert getResponseConvert() {
        return responseConvert;
    }

    public void setResponseConvert(Convert responseConvert) {
        this.responseConvert = responseConvert;
    }
}
