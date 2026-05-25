package io.github.lucklike.httpclient.dbclient.function;

import io.github.lucklike.httpclient.dbclient.function.SqlBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL构建工具使用示例
 */
public class SqlBuilderExample {

    public static void main(String[] args) {
        // 1. 简单查询
        SqlBuilder.QueryResult query1 = SqlBuilder.builder()
                .select("id", "name", "age")
                .from("user")
                .where("1 = 1")
                .eq("status", 1)
                .ge("age", 18)
                .like("name", "张")
                .orderBy("age", SqlBuilder.OrderType.DESC)
                .limit(10)
                .build();
        System.out.println("查询1: " + query1);

        // 2. 复杂查询 - 多条件组合
        SqlBuilder.QueryResult query2 = SqlBuilder.builder()
                .select("*")
                .from("user", "u")
                .leftJoin("order", "o")
                .on("u.id = o.user_id")
                .where("1 = 1")
                .eq("u.status", 1)
                .bracketStart()
                .eq("u.age", 25)
                .or()
                .eq("u.age", 30)
                .bracketEnd()
                .isNotNull("u.email")
                .groupBy("u.department")
                .having("COUNT(*) > ?", 5)
                .orderBy("COUNT(*)", SqlBuilder.OrderType.DESC)
                .build();
        System.out.println("查询2: " + query2);

        // 3. IN查询
        SqlBuilder.QueryResult query3 = SqlBuilder.builder()
                .select("id", "name")
                .from("user")
                .in("id", 1, 2, 3, 4, 5)
                .notIn("status", 0, -1)
                .build();
        System.out.println("查询3: " + query3);

        // 4. INSERT
        SqlBuilder.QueryResult insert = SqlBuilder.builder()
                .insertInto("user", "name", "email", "age")
                .values("张三", "zhangsan@example.com", 25)
                .build();
        System.out.println("插入: " + insert);

        // 5. 批量插入
        List<Object[]> batchValues = new ArrayList<>();
        batchValues.add(new Object[]{"李四", "lisi@example.com", 30});
        batchValues.add(new Object[]{"王五", "wangwu@example.com", 35});

        SqlBuilder.QueryResult batchInsert = SqlBuilder.builder()
                .insertInto("user", "name", "email", "age")
                .valuesBatch(batchValues)
                .build();
        System.out.println("批量插入: " + batchInsert);

        // 6. UPDATE
        SqlBuilder.QueryResult update = SqlBuilder.builder()
                .update("user")
                .set("name", "李四")
                .set("age", 31)
                .eq("id", 2)
                .build();
        System.out.println("更新: " + update);

        // 7. DELETE
        SqlBuilder.QueryResult delete = SqlBuilder.builder()
                .delete()
                .from("user")
                .eq("status", -1)
                .lt("create_time", "2023-01-01")
                .build();
        System.out.println("删除: " + delete);

        // 8. 子查询
        SqlBuilder subQuery = SqlBuilder.builder()
                .select("user_id")
                .from("order")
                .ge("amount", 1000);

        SqlBuilder.QueryResult query4 = SqlBuilder.builder()
                .select("*")
                .from("user")
                .in("id", subQuery.getParams()) // 实际使用需要调整
                .build();

        // 更实用的子查询 - EXISTS
        SqlBuilder.QueryResult query5 = SqlBuilder.builder()
                .select("*")
                .from("user", "u")
                .exists(
                        SqlBuilder.builder()
                                .select("1")
                                .from("order", "o")
                                .where("o.user_id = u.id")
                                .ge("o.amount", 1000)
                )
                .build();
        System.out.println("子查询: " + query5);

        // 9. 动态条件查询
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("name", "张三");
        conditions.put("status", 1);
        conditions.put("age", 25);

        SqlBuilder dynamicQuery = SqlBuilder.builder()
                .select("*")
                .from("user")
                .where("1 = 1");

        // 动态添加条件
        if (conditions.containsKey("name")) {
            dynamicQuery.eq("name", conditions.get("name"));
        }
        if (conditions.containsKey("status")) {
            dynamicQuery.eq("status", conditions.get("status"));
        }
        if (conditions.containsKey("age")) {
            dynamicQuery.ge("age", conditions.get("age"));
        }

        SqlBuilder.QueryResult dynamicResult = dynamicQuery.build();
        System.out.println("动态查询: " + dynamicResult);

        // 10. 分页查询
        int pageNum = 2;
        int pageSize = 10;
        int offset = (pageNum - 1) * pageSize;

        SqlBuilder.QueryResult pageQuery = SqlBuilder.builder()
                .select("*")
                .from("user")
                .where("1 = 1")
                .eq("status", 1)
                .orderBy("create_time", SqlBuilder.OrderType.DESC)
                .limit(offset, pageSize)
                .build();
        System.out.println("分页查询: " + pageQuery);

        // 11. BETWEEN查询
        SqlBuilder.QueryResult betweenQuery = SqlBuilder.builder()
                .select("*")
                .from("order")
                .between("amount", 100, 500)
                .between("create_time", "2024-01-01", "2024-12-31")
                .build();
        System.out.println("区间查询: " + betweenQuery);

        // 12. 使用Map批量SET
        Map<String, Object> setValues = new LinkedHashMap<>();
        setValues.put("name", "新名字");
        setValues.put("email", "newemail@example.com");
        setValues.put("updated_at", new Date());

        SqlBuilder.QueryResult batchUpdate = SqlBuilder.builder()
                .update("user")
                .set(setValues)
                .eq("id", 1)
                .build();
        System.out.println("批量更新: " + batchUpdate);
    }
}