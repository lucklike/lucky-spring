package io.github.lucklike.httpclient.config;


import com.luckyframework.httpclient.core.ssl.KeyStoreInfo;

public class KeyStoreConfiguration extends KeyStoreInfo {

    private String id;



    /**
     * 获取此KeyStore的唯一ID
     *
     * @return 此KeyStore的唯一ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置此KeyStore的唯一ID
     *
     * @param id 此KeyStore的唯一ID
     */
    public void setId(String id) {
        this.id = id;
    }
}
