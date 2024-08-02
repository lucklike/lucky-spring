package io.github.lucklike.httpclient.config;


public class SSLContextConfiguration {

    private String id;

    /**
     * 使用的协议
     */
    private String protocol = "TLS";

    /**
     * cert秘钥
     */
    private String certPass = "";

    /**
     * KeyStore类型
     */
    private String keyStoreType = "JKS";

    /**
     * KeyStore公钥文件地址
     */
    private String keyStoreFile;

    /**
     * KeyStore秘钥
     */
    private String keyStorePass;


    /**
     * 获取此SSLContext的唯一ID
     *
     * @return 此SSLContext的唯一ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置此SSLContext的唯一ID
     *
     * @param id 此SSLContext的唯一ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取使用的SSL协议，默认为TLS
     *
     * @return SSL协议
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 设置使用的SSL协议，默认为TLS
     *
     * @param protocol SSL协议
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * 获取cert秘钥
     *
     * @return cert秘钥
     */
    public String getCertPass() {
        return certPass;
    }

    /**
     * 设置cert秘钥
     *
     * @param certPass cert秘钥
     */
    public void setCertPass(String certPass) {
        this.certPass = certPass;
    }

    /**
     * 获取KeyStore类型，默认为JKS
     *
     * @return KeyStore类型
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * 设置KeyStore类型，默认为JKS
     *
     * @param keyStoreType KeyStore类型
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * 获取KeyStore公钥文件地址
     *
     * @return KeyStore公钥文件地址
     */
    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    /**
     * 设置KeyStore公钥文件地址
     *
     * @param keyStoreFile KeyStore公钥文件地址
     */
    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    /**
     * 获取KeyStore秘钥
     *
     * @return KeyStore秘钥
     */
    public String getKeyStorePass() {
        return keyStorePass;
    }

    /**
     * 设置KeyStore秘钥
     *
     * @param keyStorePass KeyStore秘钥
     */
    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }
}
