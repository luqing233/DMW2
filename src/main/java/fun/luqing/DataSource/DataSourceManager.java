package fun.luqing.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceManager {
    private static final HikariDataSource dataSource;

    // 初始化数据源
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/");  // 不指定数据库，连接到 MySQL 服务
        config.setUsername("luqing");
        config.setPassword("123456");
        config.setMaximumPoolSize(10); // 设置连接池最大连接数
        config.setMinimumIdle(5); // 设置最小空闲连接数
        config.setIdleTimeout(300000); // 设置连接空闲的最大时间
        config.setConnectionTimeout(30000); // 设置获取连接的最大等待时间
        dataSource = new HikariDataSource(config);
    }

    // 获取数据库连接并切换到指定的架构
    public static Connection getConnection(String schema) throws SQLException {
        Connection connection = dataSource.getConnection();
        return new AutoSchemaConnection(connection, schema); // 返回包装后的连接，自动切换架构
    }

    // 关闭数据源
    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
