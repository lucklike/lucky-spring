package io.github.lucklike.httpclient.config;

import com.luckyframework.httpclient.core.ssl.HostnameVerifierFactory;
import com.luckyframework.httpclient.core.ssl.SSLSocketFactoryFactory;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public class SSLConfiguration {

    /**
     * 是否全局开启SSL，为true时默认开启的时单向认证并且忽略域名校验<br/>
     * <pre>
     *     1.当hostname-verifier和ssl-socket-factory不null时使用自定义的SSL配置
     *     2.当global-ssl-context不为空时，则取对应的SSLContext进行双向认证
     *     3.未做任何配置时，使用单向认证并且忽略域名校验
     * </pre>
     */
    private Boolean globalEnable;

    /**
     * SSL主机名验证器{@link HostnameVerifier}工厂
     */
    @NestedConfigurationProperty
    private SimpleGenerateEntry<HostnameVerifierFactory> hostnameVerifier;

    /**
     * SSLSocket工厂{@link SSLSocketFactory}工厂
     */
    @NestedConfigurationProperty
    private SimpleGenerateEntry<SSLSocketFactoryFactory> sslSocketFactory;

    /**
     * 获取SSL主机名验证器{@link HostnameVerifier}的SpEL表达式
     */
    private String hostnameVerifierExpression;

    /**
     * 获取SSLSocket工厂{@link SSLSocketFactory}的SpEL表达式
     */
    private String sslSocketFactoryExpression;

    /**
     * 全局SSL协议，默认为TLS
     */
    private String globalProtocol = "TLS";

    /**
     * 全局KeyStore，如果不配做则默认使用单向认证
     */
    private String globalKeyStore;

    /**
     * 全局TrustStore，如果不配做则默忽略服务器端证书认证
     */
    private String globalTrustStore;

    /**
     * KeyStore配置
     */
    private KeyStoreConfiguration[] keyStores;

    /**
     * 是否全局开启SSL，为true时默认开启的时单向认证并且忽略域名校验<br/>
     * <pre>
     *     1.当hostname-verifier和ssl-socket-factory不null时使用自定义的SSL配置
     *     2.当global-ssl-context不为空时，则取对应的SSLContext进行双向认证
     *     3.未做任何配置时，使用单向认证并且忽略域名校验
     * </pre>
     *
     * @return 是否开启全局SSL
     */
    public Boolean getGlobalEnable() {
        return globalEnable;
    }

    /**
     * 是否全局开启SSL，为true时默认开启的时单向认证并且忽略域名校验<br/>
     * <pre>
     *     1.当hostname-verifier和ssl-socket-factory不null时使用自定义的SSL配置
     *     2.当global-ssl-context不为空时，则取对应的SSLContext进行双向认证
     *     3.未做任何配置时，使用单向认证并且忽略域名校验
     * </pre>
     *
     * @param globalEnable 是否开启全局SSL
     */
    public void setGlobalEnable(Boolean globalEnable) {
        this.globalEnable = globalEnable;
    }

    /**
     * 获取SSL主机名验证器{@link HostnameVerifier}工厂
     *
     * @return SSL主机名验证器{@link HostnameVerifier}工厂
     */
    public SimpleGenerateEntry<HostnameVerifierFactory> getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * 设置SSL主机名验证器{@link HostnameVerifier}工厂
     *
     * @param hostnameVerifier SSL主机名验证器{@link HostnameVerifier}工厂
     */
    public void setHostnameVerifier(SimpleGenerateEntry<HostnameVerifierFactory> hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    /**
     * 获取SSLSocket工厂{@link SSLSocketFactory}工厂
     *
     * @return SSLSocket工厂{@link SSLSocketFactory}工厂
     */
    public SimpleGenerateEntry<SSLSocketFactoryFactory> getSslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * 设置SSLSocket工厂{@link SSLSocketFactory}工厂
     *
     * @param sslSocketFactory SSLSocket工厂{@link SSLSocketFactory}工厂
     */
    public void setSslSocketFactory(SimpleGenerateEntry<SSLSocketFactoryFactory> sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * 获取SSL主机名验证器{@link HostnameVerifier}的SpEL表达式
     *
     * @return SSL主机名验证器{@link HostnameVerifier}的SpEL表达式
     */
    public String getHostnameVerifierExpression() {
        return hostnameVerifierExpression;
    }

    public void setHostnameVerifierExpression(String hostnameVerifierExpression) {
        this.hostnameVerifierExpression = hostnameVerifierExpression;
    }

    public String getSslSocketFactoryExpression() {
        return sslSocketFactoryExpression;
    }

    public void setSslSocketFactoryExpression(String sslSocketFactoryExpression) {
        this.sslSocketFactoryExpression = sslSocketFactoryExpression;
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
    public String getGlobalKeyStore() {
        return globalKeyStore;
    }

    /**
     * 设置全局SSL上下文ID，如果不配做则默认使用单向认证
     *
     * @param globalKeyStore 全局SSL上下文ID
     */
    public void setGlobalKeyStore(String globalKeyStore) {
        this.globalKeyStore = globalKeyStore;
    }

    public String getGlobalTrustStore() {
        return globalTrustStore;
    }

    public void setGlobalTrustStore(String globalTrustStore) {
        this.globalTrustStore = globalTrustStore;
    }

    /**
     * 获取所有SSL上下文配置
     *
     * @return 所有SSL上下文配置
     */
    public KeyStoreConfiguration[] getKeyStores() {
        return keyStores;
    }

    /**
     * 设置一组SSL上下文配置
     *
     * @param keyStores 一组SSL上下文配置
     */
    public void setKeyStores(KeyStoreConfiguration[] keyStores) {
        this.keyStores = keyStores;
    }
}
