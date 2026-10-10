package io.github.lucklike.httpclient.dbclient.function;

import io.github.lucklike.httpclient.dbclient.annotation.AuditFillScene;

import java.util.Collections;
import java.util.List;

/**
 * 默认 SpEL 填充处理器。
 * <p>
 * 实现 {@link FillHandler} 接口，提供对 {@link DefaultSpELFiller} 的路由能力。
 * 当没有注册任何自定义 FillHandler 时，此 Handler 作为默认的 SpEL 表达式求值器。
 * </p>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/10/10
 * @see DefaultSpELFiller
 * @see FillRegistry
 */
public class DefaultSpELFillHandler implements FillHandler {

    /**
     * 单例填充器，无状态可安全复用
     */
    private static final DefaultSpELFiller FILLER = new DefaultSpELFiller();

    /**
     * 获取指定实体类和场景下的 SpEL 填充器列表。
     * <p>
     * 返回包含单个 DefaultSpELFiller 的不可变列表。
     * </p>
     *
     * @param entityClass 实体类类型（仅用于元数据查找，不影响填充逻辑）
     * @param scene       填充场景
     * @return 包含 DefaultSpELFiller 的列表
     */
    @Override
    public List<Filler> getFillers(Class<?> entityClass, AuditFillScene scene) {
        return Collections.singletonList(FILLER);
    }

    @Override
    public boolean needsBackfill() {
        return true; // 单条 insert/updateById 场景需要回写实体供 columnHandler 采集
    }
}
