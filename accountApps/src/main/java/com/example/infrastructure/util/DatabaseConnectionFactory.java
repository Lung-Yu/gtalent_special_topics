package com.example.infrastructure.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 資料庫連接工廠
 * 使用 HikariCP 連接池管理資料庫連接
 */
public class DatabaseConnectionFactory {
    
    private static HikariDataSource dataSource;
    
    // 資料庫連接參數
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/accountapps?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "student";
    private static final String PASSWORD = "student123";
    
    // 連接池配置
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int MINIMUM_IDLE = 2;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 秒
    private static final long IDLE_TIMEOUT = 600000; // 10 分鐘
    private static final long MAX_LIFETIME = 1800000; // 30 分鐘
    
    /**
     * 私有建構子，防止實例化
     */
    private DatabaseConnectionFactory() {
        throw new IllegalStateException("Utility class - cannot instantiate");
    }
    
    /**
     * 初始化資料來源（連接池）
     */
    private static synchronized void initializeDataSource() {
        if (dataSource == null) {
            try {
                HikariConfig config = new HikariConfig();
                
                // 基本配置
                config.setJdbcUrl(JDBC_URL);
                config.setUsername(USERNAME);
                config.setPassword(PASSWORD);
                
                // 連接池配置
                config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
                config.setMinimumIdle(MINIMUM_IDLE);
                config.setConnectionTimeout(CONNECTION_TIMEOUT);
                config.setIdleTimeout(IDLE_TIMEOUT);
                config.setMaxLifetime(MAX_LIFETIME);
                
                // 連接測試
                config.setConnectionTestQuery("SELECT 1");
                
                // 連接池名稱
                config.setPoolName("AccountApps-HikariCP");
                
                // 性能優化
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("useLocalSessionState", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");
                config.addDataSourceProperty("cacheResultSetMetadata", "true");
                config.addDataSourceProperty("cacheServerConfiguration", "true");
                config.addDataSourceProperty("elideSetAutoCommits", "true");
                config.addDataSourceProperty("maintainTimeStats", "false");
                
                dataSource = new HikariDataSource(config);
                
                System.out.println("✓ 資料庫連接池初始化成功");
                
            } catch (Exception e) {
                System.err.println("✗ 資料庫連接池初始化失敗：" + e.getMessage());
                throw new RuntimeException("Failed to initialize database connection pool", e);
            }
        }
    }
    
    /**
     * 取得資料庫連接
     * 
     * @return Connection 物件
     * @throws SQLException 當無法取得連接時
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initializeDataSource();
        }
        return dataSource.getConnection();
    }
    
    /**
     * 關閉連接池
     * 應在應用程式關閉時呼叫
     */
    public static synchronized void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
            System.out.println("✓ 資料庫連接池已關閉");
        }
    }
    
    /**
     * 檢查連接池是否已初始化
     * 
     * @return true 如果已初始化，否則 false
     */
    public static boolean isInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }
    
    /**
     * 取得連接池狀態資訊
     * 
     * @return 連接池狀態字串
     */
    public static String getPoolStatus() {
        if (dataSource == null) {
            return "連接池未初始化";
        }
        
        return String.format(
            "連接池狀態 - 活動連接: %d, 閒置連接: %d, 總連接: %d, 等待中: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
}
