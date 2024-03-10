package io.github.lucklike.httpclient.config;

/**
 * 简单生成器实体
 * @author fukang
 * @version 1.0.0
 * @date 2024/3/9 16:37
 */
public class SimpleGenerateEntry<T> {

    /**
     * 生成器需要生成对象的Class
     */
    private Class<T> type;
    /**
     * 生成该对象所需要的额外信息
     */
    private String msg = "";


    /**
     * 生成对象的Class
     *
     * @return 生成对象的Class
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * 设置生成对象的Class
     *
     * @param type 生成对象的Class
     */
    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     * 获取生成对象需要用到的额外信息
     *
     * @return 生成对象需要用到的额外信息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置生成对象需要用到的额外信息
     *
     * @param msg 生成对象需要用到的额外信息
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
