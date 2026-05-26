package io.github.lucklike.httpclient.dbclient.sql;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 支持序列化的 Function，参照 MyBatis-Plus 实现
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/25 02:20
 */
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
