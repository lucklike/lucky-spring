package io.github.lucklike.httpclient.extend;

import com.luckyframework.conversion.TargetField;

/**
 * @author fukang
 * @version 1.0.0
 * @date 2024/6/30 16:15
 */
public class Body {

    @TargetField("mime-type")
    private String mimeType;
    private String charset;
    private String data;
    private String file;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
