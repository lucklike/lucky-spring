package io.github.lucklike.httpclient.dbclient.function;

import io.github.lucklike.httpclient.dbclient.sql.SqlBuilder;

/**
 * 实体条件拼接类
 *
 * @author fukang
 * @version 1.0.0
 * @date 2026/5/31 21:24
 */
@FunctionalInterface
public interface Condition {

    /**
     * 追加条件
     *
     * @param sqlBuilder SQL 构建着
     * @param columnInfo 列信息
     */
    void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo);

    /**
     * 等于条件
     */
    class Eq implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            sqlBuilder.eq(columnInfo.getName(), columnInfo.getValue());
        }
    }

    /**
     * 不等于条件
     */
    class Ne implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            sqlBuilder.ne(columnInfo.getName(), columnInfo.getValue());
        }
    }

    /**
     * 大于条件
     */
    class Gt implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            sqlBuilder.gt(columnInfo.getName(), columnInfo.getValue());
        }
    }

    /**
     * 大于等于条件
     */
    class Ge implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            sqlBuilder.ge(columnInfo.getName(), columnInfo.getValue());
        }
    }

    /**
     * 小于条件
     */
    class Lt implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            sqlBuilder.lt(columnInfo.getName(), columnInfo.getValue());
        }
    }

    /**
     * 小于等于条件
     */
    class Le implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            sqlBuilder.le(columnInfo.getName(), columnInfo.getValue());
        }
    }

    /**
     * 模糊查询条件（前后都加%）
     */
    class Like implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            String value = String.valueOf(columnInfo.getValue());
            sqlBuilder.like(columnInfo.getName(), value);
        }
    }

    /**
     * 左模糊查询条件（前加%）
     */
    class LikeLeft implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            String value = String.valueOf(columnInfo.getValue());
            sqlBuilder.likeLeft(columnInfo.getName(), value);
        }
    }

    /**
     * 右模糊查询条件（后加%）
     */
    class LikeRight implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            String value = String.valueOf(columnInfo.getValue());
            sqlBuilder.likeRight(columnInfo.getName(), value);
        }
    }

    /**
     * IS NULL 条件
     */
    class IsNull implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            sqlBuilder.isNull(columnInfo.getName());
        }
    }

    /**
     * IS NOT NULL 条件
     */
    class IsNotNull implements Condition {
        @Override
        public void additionCondition(SqlBuilder sqlBuilder, ColumnInfo columnInfo) {
            sqlBuilder.isNotNull(columnInfo.getName());
        }
    }
}