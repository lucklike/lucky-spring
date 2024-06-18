package io.github.lucklike.httpclient.generalapi;

import com.luckyframework.httpclient.core.meta.Response;
import com.luckyframework.httpclient.proxy.spel.FunctionFilter;

public class RangeInfo {

    private static final RangeInfo NOT_SUPPORT = new RangeInfo(false, null, -1L);

    private final boolean support;
    private final long length;
    private final String filename;

    private RangeInfo(boolean support, String filename, long length) {
        this.support = support;
        this.length = length;
        this.filename = filename;
    }

    public boolean isSupport() {
        return support;
    }

    public long getLength() {
        return length;
    }

    public String getFilename() {
        return filename;
    }

    @FunctionFilter
    public static RangeInfo create(String filename, long length) {
        return new RangeInfo(true, filename, length);
    }

    public static RangeInfo create(Response response) {
        String contentRang = String.valueOf(response.getSimpleHeaders().get("Content-Range"));
        String filename = response.getResponseMetaData().getDownloadFilename();
        return create(filename, Long.parseLong(contentRang.split("/")[1]));
    }

    public static RangeInfo notSupport() {
        return NOT_SUPPORT;
    }

}
