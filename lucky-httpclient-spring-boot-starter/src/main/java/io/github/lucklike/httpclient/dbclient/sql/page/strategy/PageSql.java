package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

/**
 * 分页SQL及参数封装类
 * <p>
 * 封装分页查询的SQL语句和对应的参数，用于数据库分页查询执行
 *
 * <p><b>使用场景：</b>
 * <pre>{@code
 * // 由分页策略生成
 * PageSql pageSql = strategy.pageSql("SELECT * FROM user", page);
 *
 * // 执行分页查询
 * jdbcTemplate.query(pageSql.getSql(), pageSql.getPageParam(), rowMapper);
 * }</pre>
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 09:46
 */
public class PageSql {

    /**
     * 分页查询SQL语句
     * 已根据数据库方言转换，包含分页参数占位符
     */
    private final String sql;

    /**
     * 分页参数数组
     * 顺序对应SQL中的占位符，通常包含offset、limit或startRow、endRow等参数
     */
    private final Object[] pageParam;

    /**
     * 构造分页SQL对象
     *
     * @param sql       分页查询SQL语句
     * @param pageParam 分页参数数组
     */
    public PageSql(String sql, Object[] pageParam) {
        this.sql = sql;
        this.pageParam = pageParam;
    }

    /**
     * 获取分页查询SQL语句
     *
     * @return 分页SQL语句
     */
    public String getSql() {
        return sql;
    }

    /**
     * 获取分页参数数组
     *
     * @return 分页参数数组
     */
    public Object[] getPageParam() {
        return pageParam;
    }

    @Override
    public String toString() {
        return "PageSql{" +
                "sql='" + sql + '\'' +
                ", pageParam=" + java.util.Arrays.toString(pageParam) +
                '}';
    }
}