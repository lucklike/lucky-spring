package io.github.lucklike.httpclient.config.simple;

public class SimpleBody {
    /**
     * 文本类型的请求体
     */
    private String txt;

    /**
     * 文件类型的请求体
     */
    private String file;

    /**
     * 获取文本类型的请求体
     *
     * @return 文本类型的请求体
     */
    public String getTxt() {
        return txt;
    }

    /**
     * 文本类型的请求体
     *
     * @param txt 文本类型的请求体
     */
    public void setTxt(String txt) {
        this.txt = txt;
    }

    /**
     * 获取文件类型的请求体
     *
     * @return 文件类型的请求体
     */
    public String getFile() {
        return file;
    }

    /**
     * 文件类型的请求体
     *
     * @param file 文件类型的请求体
     */
    public void setFile(String file) {
        this.file = file;
    }
}
