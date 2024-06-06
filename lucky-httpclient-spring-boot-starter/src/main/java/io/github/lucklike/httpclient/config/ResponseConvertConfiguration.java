package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.core.Response;
import com.luckyframework.httpclient.core.impl.ContentEncodingConvertor;

/**
 * 响应内容转换相关配置
 */
public class ResponseConvertConfiguration {

    /**
     * 响应结果自动转换器
     */
    private Class<? extends Response.AutoConvert>[] responseAutoConverts;

    /**
     * 响应内容解码器
     */
    private Class<? extends ContentEncodingConvertor>[] contentEncodingDecoder;

    /**
     * 是否启用压缩内容自动解压功能
     */
    private boolean enableContentCompress;

    /**
     * 客户端支持的压缩格式，<b>enable-content-compress</b>功能开启时生效<br/>
     * 参照HTTP请求头规范中的<b>Accept-Encoding</b><br/>
     */
    private String acceptEncoding;

    /**
     * 设置响应结果自动转换器
     *
     * @param responseAutoConverts 应结果自动转换器
     */
    public void setResponseAutoConverts(Class<? extends Response.AutoConvert>[] responseAutoConverts) {
        this.responseAutoConverts = responseAutoConverts;
    }

    /**
     * 设置响应内容解码器
     *
     * @param contentEncodingDecoder 响应内容解码器
     */
    public void setContentEncodingDecoder(Class<? extends ContentEncodingConvertor>[] contentEncodingDecoder) {
        this.contentEncodingDecoder = contentEncodingDecoder;
    }

    /**
     * 设置是否开启压缩内容自动解压功能
     *
     * @param enableContentCompress 是否开启压缩内容自动解压功能
     */
    public void setEnableContentCompress(boolean enableContentCompress) {
        this.enableContentCompress = enableContentCompress;
    }

    /**
     * 设置HTTP请求头规范中的<b>Accept-Encoding</b>
     *
     * @param acceptEncoding <b>Accept-Encoding</b>值
     */
    public void setAcceptEncoding(String acceptEncoding) {
        this.acceptEncoding = acceptEncoding;
    }


    /**
     * 获取配置的应结果自动转换器数组
     *
     * @return 应结果自动转换器数组
     */
    public Class<? extends Response.AutoConvert>[] getResponseAutoConverts() {
        return responseAutoConverts;
    }

    /**
     * 获取响应内容解码器
     *
     * @return 响应内容解码器
     */
    public Class<? extends ContentEncodingConvertor>[] getContentEncodingDecoder() {
        return contentEncodingDecoder;
    }

    /**
     * 是否开启了压缩内容自动解压功能
     *
     * @return 是否开启了压缩内容自动解压功能
     */
    public boolean isEnableContentCompress() {
        return enableContentCompress;
    }

    /**
     * 获取<b>Accept-Encoding</b>配置值
     *
     * @return <b>Accept-Encoding</b>配置值
     */
    public String getAcceptEncoding() {
        return acceptEncoding;
    }

}
