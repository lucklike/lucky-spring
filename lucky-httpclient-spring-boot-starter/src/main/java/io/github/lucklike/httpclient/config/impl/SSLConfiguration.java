package io.github.lucklike.httpclient.config.impl;

public class SSLConfiguration {

    /**
     * 是否全局开启SSL，为true时默认开启的时单向认证并且忽略域名校验，当globalSslContext不为空时，则取对应的SSLContext进行双向认证
     */
    private Boolean globalEnable;

    /**
     * 全局SSL协议，默认为TLS
     */
    private String globalProtocol = "TLS";

    /**
     * 全局SSL上下文ID，如果不配做则默认使用单向认证
     */
    private String globalSslContext;

    /**
     * SSL上下文配置
     */
    private SSLContextConfiguration[] sslContexts;

    /**
     * 是否开启全局SSL，为true时默认开启的时单向认证并且忽略域名校验，当globalSslContext不为空时，则取对应的SSLContext进行双向认证
     *
     * @return 是否开启全局SSL
     */
    public Boolean getGlobalEnable() {
        return globalEnable;
    }

    /**
     * 设置是否开启全局SSL，为true时默认开启的时单向认证并且忽略域名校验，当globalSslContext不为空时，则取对应的SSLContext进行双向认证
     *
     * @param globalEnable 是否开启全局SSL
     */
    public void setGlobalEnable(Boolean globalEnable) {
        this.globalEnable = globalEnable;
    }

    /**
     * 获取全局SSL协议
     *
     * @return 全局SSL协议
     */
    public String getGlobalProtocol() {
        return globalProtocol;
    }

    /**
     * 设置全局SSL协议
     *
     * @param globalProtocol 全局SSL协议
     */
    public void setGlobalProtocol(String globalProtocol) {
        this.globalProtocol = globalProtocol;
    }

    /**
     * 获取全局SSL上下文ID，如果不配做则默认使用单向认证
     *
     * @return 全局SSL上下文ID
     */
    public String getGlobalSslContext() {
        return globalSslContext;
    }

    /**
     * 设置全局SSL上下文ID，如果不配做则默认使用单向认证
     *
     * @param globalSslContext 全局SSL上下文ID
     */
    public void setGlobalSslContext(String globalSslContext) {
        this.globalSslContext = globalSslContext;
    }

    /**
     * 获取所有SSL上下文配置
     *
     * @return 所有SSL上下文配置
     */
    public SSLContextConfiguration[] getSslContexts() {
        return sslContexts;
    }

    /**
     * 设置一组SSL上下文配置
     *
     * @param sslContexts 一组SSL上下文配置
     */
    public void setSslContexts(SSLContextConfiguration[] sslContexts) {
        this.sslContexts = sslContexts;
    }
}
