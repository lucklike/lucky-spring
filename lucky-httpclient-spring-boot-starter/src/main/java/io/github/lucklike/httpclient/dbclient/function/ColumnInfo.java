package io.github.lucklike.httpclient.dbclient.function;

/**
 * 列信息内部类
 * <p>封装数据库列的元数据信息，包括列名、字段值和是否为主键标识</p>
 */
public class ColumnInfo {
    /**
     * 数据库列名
     */
    private final String name;
    /**
     * 字段值
     */
    private final Object value;
    /**
     * 是否为主键ID字段
     */
    private final boolean isId;

    /**
     * 列查询条件拼接
     */
    private final Condition condition;

    /**
     * 构造列信息对象
     *
     * @param name  数据库列名
     * @param value 字段值
     * @param isId  是否为主键ID字段
     */
    public ColumnInfo(String name, Object value, boolean isId, Condition condition) {
        this.name = name;
        this.value = value;
        this.isId = isId;
        this.condition = condition;
    }

    /**
     * 获取数据库列名
     *
     * @return 列名
     */
    public String getName() {
        return name;
    }

    /**
     * 获取字段值
     *
     * @return 字段值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 判断是否为主键ID字段
     *
     * @return true表示是ID字段，false表示不是
     */
    public boolean isId() {
        return isId;
    }

    /**
     * 获取查询条件拼接器
     *
     * @return 查询条件拼接器
     */
    public Condition getCondition() {
        return condition;
    }
}