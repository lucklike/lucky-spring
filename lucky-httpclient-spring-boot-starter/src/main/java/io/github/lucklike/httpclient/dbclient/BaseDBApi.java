package io.github.lucklike.httpclient.dbclient;

import io.github.lucklike.httpclient.dbclient.sql.lambda.Lambda;
import org.springframework.core.ResolvableType;

/**
 * 提供基于实体类的 CRUD 操作以及批量操作的核心数据库访问接口。
 * <p>
 * 该接口是数据库客户端的核心门面，聚合了以下能力接口的全部能力：
 * <ul>
 *     <li>{@link QueryApi} - COUNT 统计、单条/列表/分页/流式查询</li>
 *     <li>{@link WriteApi} - 插入、更新、删除、批量操作、saveOrUpdate</li>
 *     <li>{@link SingleColumnApi} - 指定列的列表/流式/分页查询</li>
 *     <li>{@link LambdaBuilderApi} - 各类 Lambda 构建器的创建入口</li>
 * </ul>
 * 提供了丰富的数据访问方法，包括：
 * <ul>
 *     <li>Lambda 表达式构建动态查询（推荐使用 {@link Lambda} 入口类）</li>
 *     <li>实体对象作为查询条件（零 SQL 配置）</li>
 *     <li>分页查询与流式查询</li>
 *     <li>单条/批量插入、更新、删除</li>
 *     <li>根据 ID 的快捷操作</li>
 *     <li>保存或更新（saveOrUpdate）语义</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 1. 通过依赖注入获取实例
 * \@Autowired
 * private BaseDBApi<User> userDBApi;
 *
 * // 2. Lambda 条件查询（使用 Lambda 入口类）
 * List<User> users = userDBApi.selectList(
 *     Lambda.select(User.class)
 *         .where(User::getStatus).eq(1)
 *         .orderByDesc(User::getCreateTime)
 * );
 *
 * // 3. 实体对象条件查询
 * User condition = new User();
 * condition.setStatus(1);
 * condition.setAge(18);
 * List<User> users = userDBApi.selectList(condition);
 *
 * // 4. 分页查询
 * Page page = Page.of(1, 10).desc("create_time");
 * PageResult<User> result = userDBApi.selectPage(condition, page);
 *
 * // 5. 插入/更新
 * User user = new User();
 * user.setName("张三");
 * user.setAge(18);
 * userDBApi.insert(user);
 * userDBApi.updateById(user);
 *
 * // 6. 批量操作
 * List<User> userList = getUsers();
 * userDBApi.batchInsert(userList);
 * }
 * </pre>
 * </p>
 *
 * @param <E> 实体类型泛型
 * @author fukang
 * @version 1.0.0
 */
public interface BaseDBApi<E> extends WriteApi<E>, SingleColumnApi<E>, LambdaBuilderApi<E> {

    /**
     * 获取实体类类型。
     * <p>
     * 通过泛型参数自动解析实体类的 Class 对象。
     * </p>
     *
     * @return 实体类 Class 对象
     */
    @Override
    @SuppressWarnings("unchecked")
    default Class<E> entityClass() {
        return (Class<E>) ResolvableType.forClass(BaseDBApi.class, getClass()).getGeneric(0).toClass();
    }
}
