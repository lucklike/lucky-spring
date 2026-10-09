package io.github.lucklike.httpclient.dbclient.sql.page.strategy;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 分页策略工厂
 * 根据数据库类型获取对应的分页策略
 */
public class PageStrategyFactory {

    /**
     * DataSource 与分页策略的缓存，避免每次分页查询都探测数据库元数据
     */
    private static final Map<DataSource, PageStrategy> DATA_SOURCE_STRATEGY_CACHE = new ConcurrentHashMap<>();

    /**
     * 自定义分页策略注册表（key为小写的数据库产品关键字）
     */
    private static final Map<String, Supplier<PageStrategy>> CUSTOM_STRATEGIES = new ConcurrentHashMap<>();

    /**
     * 数据库类型枚举
     */
    public enum DatabaseType {
        MYSQL,
        POSTGRESQL,
        ORACLE,
        ORACLE_12C,
        SQL_SERVER,
        SQL_SERVER_2008,
        SQLITE,
        H2,
        DB2,
        DAMENG
    }

    /**
     * 根据数据库类型获取分页策略
     */
    public static PageStrategy getStrategy(DatabaseType type) {
        switch (type) {
            case MYSQL:
                return new MySqlPageStrategy();
            case POSTGRESQL:
                return new PostgreSqlPageStrategy();
            case ORACLE:
                return new OraclePageStrategy();
            case ORACLE_12C:
                return new Oracle12cPageStrategy();
            case SQL_SERVER:
                return new SqlServerPageStrategy();
            case SQL_SERVER_2008:
                return new SqlServer2008PageStrategy();
            case SQLITE:
                return new SqlitePageStrategy();
            case H2:
                return new H2PageStrategy();
            case DB2:
                return new Db2PageStrategy();
            case DAMENG:
                return new DamengPageStrategy();
            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }

    /**
     * 注册自定义分页策略，当数据库产品名包含 keyword（不区分大小写）时使用该策略
     * <p>通常在内置策略不支持的数据库（如人大金仓等）场景下使用</p>
     *
     * @param keyword  数据库产品关键字，如 "kingbase"
     * @param supplier 分页策略提供者
     */
    public static void registerStrategy(String keyword, Supplier<PageStrategy> supplier) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Database keyword cannot be null or empty");
        }
        CUSTOM_STRATEGIES.put(keyword.trim().toLowerCase(), supplier);
    }

    /**
     * 根据标准数据库名称获取分页策略
     *
     * @param standardDatabaseName 标准数据库名称（如：MySQL, PostgreSQL, Oracle, SQL Server等）
     * @return 对应的分页策略
     */
    public static PageStrategy getStrategyByStandardName(String standardDatabaseName) {
        if (standardDatabaseName == null || standardDatabaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }

        String lowerName = standardDatabaseName.toLowerCase();

        // 优先使用用户注册的自定义策略
        for (Map.Entry<String, Supplier<PageStrategy>> entry : CUSTOM_STRATEGIES.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return entry.getValue().get();
            }
        }

        if (lowerName.contains("mysql")) {
            return getStrategy(DatabaseType.MYSQL);
        } else if (lowerName.contains("postgresql") || lowerName.contains("postgres")) {
            return getStrategy(DatabaseType.POSTGRESQL);
        } else if (lowerName.contains("oracle")) {
            return getStrategy(DatabaseType.ORACLE);
        } else if (lowerName.contains("sql server") || lowerName.contains("sqlserver")) {
            return getStrategy(DatabaseType.SQL_SERVER);
        } else if (lowerName.contains("sqlite")) {
            return getStrategy(DatabaseType.SQLITE);
        } else if (lowerName.contains("h2")) {
            return getStrategy(DatabaseType.H2);
        } else if (lowerName.contains("db2")) {
            return getStrategy(DatabaseType.DB2);
        } else if (lowerName.contains("dameng") || lowerName.contains("dm")) {
            return getStrategy(DatabaseType.DAMENG);
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + standardDatabaseName);
        }
    }

    /**
     * 根据标准数据库名称和版本号获取分页策略
     *
     * @param standardDatabaseName 标准数据库名称
     * @param version 数据库版本号
     * @return 对应的分页策略
     */
    public static PageStrategy getStrategyByStandardName(String standardDatabaseName, String version) {
        if (standardDatabaseName == null || standardDatabaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }

        String lowerName = standardDatabaseName.toLowerCase();

        if (lowerName.contains("oracle") && version != null) {
            try {
                int majorVersion = Integer.parseInt(version.split("\\.")[0]);
                if (majorVersion >= 12) {
                    return getStrategy(DatabaseType.ORACLE_12C);
                }
            } catch (NumberFormatException e) {
                // 忽略版本解析错误，使用默认策略
            }
            return getStrategy(DatabaseType.ORACLE);
        }

        if (lowerName.contains("sql server") && version != null) {
            try {
                // SQL Server 版本号如：15.0.2000.5 (SQL Server 2019)
                int majorVersion = Integer.parseInt(version.split("\\.")[0]);
                if (majorVersion >= 12) { // SQL Server 2014 对应版本号 12
                    return getStrategy(DatabaseType.SQL_SERVER);
                }
            } catch (NumberFormatException e) {
                // 忽略版本解析错误，使用默认策略
            }
            return getStrategy(DatabaseType.SQL_SERVER_2008);
        }

        return getStrategyByStandardName(standardDatabaseName);
    }

    /**
     * 根据 JDBC URL 自动识别数据库类型并获取策略
     */
    public static PageStrategy getStrategyByJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            throw new IllegalArgumentException("JDBC URL cannot be null");
        }

        String lowerUrl = jdbcUrl.toLowerCase();

        if (lowerUrl.contains("mysql")) {
            return getStrategy(DatabaseType.MYSQL);
        } else if (lowerUrl.contains("postgresql")) {
            return getStrategy(DatabaseType.POSTGRESQL);
        } else if (lowerUrl.contains("oracle")) {
            return getStrategy(DatabaseType.ORACLE);
        } else if (lowerUrl.contains("sqlserver")) {
            return getStrategy(DatabaseType.SQL_SERVER);
        } else if (lowerUrl.contains("sqlite")) {
            return getStrategy(DatabaseType.SQLITE);
        } else if (lowerUrl.contains("h2")) {
            return getStrategy(DatabaseType.H2);
        } else if (lowerUrl.contains("db2")) {
            return getStrategy(DatabaseType.DB2);
        } else if (lowerUrl.contains("dameng") || lowerUrl.contains("dm")) {
            return getStrategy(DatabaseType.DAMENG);
        } else {
            throw new IllegalArgumentException("Unsupported database type for URL: " + jdbcUrl);
        }
    }

    /**
     * 根据 JDBC URL 和版本号获取策略
     */
    public static PageStrategy getStrategyByJdbcUrl(String jdbcUrl, String version) {
        if (jdbcUrl == null) {
            throw new IllegalArgumentException("JDBC URL cannot be null");
        }

        String lowerUrl = jdbcUrl.toLowerCase();

        if (lowerUrl.contains("oracle") && version != null) {
            try {
                int majorVersion = Integer.parseInt(version.split("\\.")[0]);
                if (majorVersion >= 12) {
                    return getStrategy(DatabaseType.ORACLE_12C);
                }
            } catch (NumberFormatException e) {
                // 忽略版本解析错误，使用默认策略
            }
            return getStrategy(DatabaseType.ORACLE);
        }

        if (lowerUrl.contains("sqlserver") && version != null) {
            try {
                int majorVersion = Integer.parseInt(version.split("\\.")[0]);
                if (majorVersion >= 2012) {
                    return getStrategy(DatabaseType.SQL_SERVER);
                }
            } catch (NumberFormatException e) {
                // 忽略版本解析错误，使用默认策略
            }
            return getStrategy(DatabaseType.SQL_SERVER_2008);
        }

        return getStrategyByJdbcUrl(jdbcUrl);
    }

    /**
     * 通过 DataSource 获取分页策略（推荐使用）
     * 自动识别数据库类型，并根据版本选择最优策略；识别结果按 DataSource 缓存
     *
     * @param dataSource 数据源
     * @return 对应的分页策略
     */
    public static PageStrategy getStrategyByDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        return DATA_SOURCE_STRATEGY_CACHE.computeIfAbsent(dataSource, PageStrategyFactory::resolveStrategyByDataSource);
    }

    /**
     * 探测数据库元数据并解析分页策略
     *
     * @param dataSource 数据源
     * @return 对应的分页策略
     */
    private static PageStrategy resolveStrategyByDataSource(DataSource dataSource) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String databaseVersion = connection.getMetaData().getDatabaseProductVersion();

            String standardName = JdbcUtils.commonDatabaseName(databaseProductName);
            return getStrategyByStandardName(standardName, databaseVersion);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database metadata from DataSource", e);
        } finally {
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    /**
     * 通过 DataSource 获取分页策略（自动关闭连接版本）
     * 使用此方法时需要确保 DataSource 支持获取连接后自动关闭
     *
     * @param dataSource 数据源
     * @param autoClose 是否自动关闭连接
     * @return 对应的分页策略
     */
    public static PageStrategy getStrategyByDataSource(DataSource dataSource, boolean autoClose) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }

        if (!autoClose) {
            return getStrategyByDataSource(dataSource);
        }

        try (Connection connection = dataSource.getConnection()) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String databaseVersion = connection.getMetaData().getDatabaseProductVersion();

            String standardName = JdbcUtils.commonDatabaseName(databaseProductName);
            return getStrategyByStandardName(standardName, databaseVersion);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database metadata from DataSource", e);
        }
    }

    /**
     * 通过 Connection 获取分页策略
     *
     * @param connection 数据库连接
     * @return 对应的分页策略
     */
    public static PageStrategy getStrategyByConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String databaseVersion = connection.getMetaData().getDatabaseProductVersion();

            String standardName = JdbcUtils.commonDatabaseName(databaseProductName);
            return getStrategyByStandardName(standardName, databaseVersion);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database metadata from Connection", e);
        }
    }

    public static String getStandardDatabaseName(DataSource dataSource) {
        if (dataSource == null) {
            return "Unknown";
        }

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return JdbcUtils.commonDatabaseName(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            // 处理异常
            return "Error";
        } finally {
            // 务必释放连接
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}