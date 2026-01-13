package io.github.lucklike.httpclient.masker;

import com.luckyframework.httpclient.proxy.logging.CustomMasker;

import java.util.Set;

/**
 * 绑定 Key 的自定义脱敏处理器
 */
public interface BindingKeyMasker extends CustomMasker {

    /**
     * 获取当前脱敏处理器作用的 key 的集合
     *
     * @return 当前脱敏处理器作用的 key 的集合
     */
    Set<String> getKeys();
}
