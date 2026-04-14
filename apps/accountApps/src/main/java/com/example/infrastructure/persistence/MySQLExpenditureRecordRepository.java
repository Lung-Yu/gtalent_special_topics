package com.example.infrastructure.persistence;

import com.example.application.exception.DatabaseConnectionException;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.valueobject.Cursor;
import com.example.domain.valueobject.PageResult;
import com.example.domain.valueobject.PaymentMethod;
import com.example.domain.valueobject.StatisticsCategory;
import com.example.domain.valueobject.UserIdentity;
import com.example.infrastructure.util.DatabaseConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL 資料庫實作的支出記錄儲存庫
 * 負責將支出記錄資料儲存至 MySQL 資料庫並從中讀取
 * 支援高效能的聚合查詢，避免在記憶體中處理大量資料
 */
public class MySQLExpenditureRecordRepository implements ExpenditureRecordRepository {
    
    private final UserRepository userRepository;
    
    /**
     * 內部類：用於在分頁查詢中保存記錄和其資料庫 ID
     */
    private static class RecordWithId {
        final ExpenditureRecord record;
        final long id;
        
        RecordWithId(ExpenditureRecord record, long id) {
            this.record = record;
            this.id = id;
        }
    }
    
    public MySQLExpenditureRecordRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date) {
        if (user == null || date == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT e.id, e.username, e.name, e.money, e.payment_method, e.date " +
                     "FROM expenditure_records e " +
                     "WHERE e.username = ? AND e.date = ?";
        
        return executeQueryWithCategories(sql, user.getUsername(), Date.valueOf(date));
    }

    @Override
    public List<ExpenditureRecord> findByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT e.id, e.username, e.name, e.money, e.payment_method, e.date " +
                     "FROM expenditure_records e " +
                     "WHERE e.date = ?";
        
        return executeQueryWithCategories(sql, Date.valueOf(date));
    }

    @Override
    public List<ExpenditureRecord> findByUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT e.id, e.username, e.name, e.money, e.payment_method, e.date " +
                     "FROM expenditure_records e " +
                     "WHERE e.username = ?";
        
        return executeQueryWithCategories(sql, user.getUsername());
    }

    @Override
    public List<ExpenditureRecord> findAll() {
        String sql = "SELECT e.id, e.username, e.name, e.money, e.payment_method, e.date " +
                     "FROM expenditure_records e";
        
        return executeQueryWithCategories(sql);
    }

    /**
     * 執行查詢並載入關聯的分類資料
     */
    private List<ExpenditureRecord> executeQueryWithCategories(String sql, Object... params) {
        List<ExpenditureRecord> records = new ArrayList<>();
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 設定參數
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String name = rs.getString("name");
                    int money = rs.getInt("money");
                    String paymentMethodStr = rs.getString("payment_method");
                    Date date = rs.getDate("date");
                    
                    // 轉換 PaymentMethod
                    PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
                    
                    // 查詢該記錄的所有分類
                    List<String> categories = findCategoriesByExpenditureId(conn, id);
                    
                    // 使用 UserIdentity 避免額外的資料庫查詢（解決 N+1 問題）
                    UserIdentity userIdentity = UserIdentity.of(username);
                    
                    ExpenditureRecord record = new ExpenditureRecord(
                        userIdentity, name, money, categories, paymentMethod, date.toLocalDate()
                    );
                    records.add(record);
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                "查詢支出記錄失敗: " + e.getMessage(), e);
        }
        
        return records;
    }
    
    /**
     * 查詢特定支出記錄的所有分類
     */
    private List<String> findCategoriesByExpenditureId(Connection conn, long expenditureId) 
            throws SQLException {
        List<String> categories = new ArrayList<>();
        
        String sql = "SELECT category_name FROM expenditure_categories WHERE expenditure_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, expenditureId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(rs.getString("category_name"));
                }
            }
        }
        
        return categories;
    }

    @Override
    public void save(ExpenditureRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("ExpenditureRecord cannot be null");
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnectionFactory.getConnection();
            conn.setAutoCommit(false); // 開啟事務
            
            // 1. 插入主記錄
            String insertRecordSql = "INSERT INTO expenditure_records " +
                                    "(username, name, money, payment_method, date) " +
                                    "VALUES (?, ?, ?, ?, ?)";
            
            long expenditureId;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    insertRecordSql, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setString(1, record.getUsername());
                pstmt.setString(2, record.getName());
                pstmt.setInt(3, record.getMoney());
                pstmt.setString(4, record.getPayway().name());
                pstmt.setDate(5, Date.valueOf(record.getDate()));
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    throw new DatabaseConnectionException("插入支出記錄失敗，沒有資料被影響");
                }
                
                // 取得自動生成的 ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        expenditureId = generatedKeys.getLong(1);
                    } else {
                        throw new DatabaseConnectionException("插入支出記錄失敗，無法取得 ID");
                    }
                }
            }
            
            // 2. 插入分類記錄
            if (record.getCategory() != null && !record.getCategory().isEmpty()) {
                String insertCategorySql = "INSERT INTO expenditure_categories " +
                                          "(expenditure_id, category_name) VALUES (?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(insertCategorySql)) {
                    for (String category : record.getCategory()) {
                        pstmt.setLong(1, expenditureId);
                        pstmt.setString(2, category);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }
            
            conn.commit(); // 提交事務
            System.out.println("✓ 支出記錄儲存成功 (ID: " + expenditureId + ")");
            
        } catch (SQLException e) {
            // 回滾事務
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new DatabaseConnectionException(
                        "回滾事務失敗: " + ex.getMessage(), ex);
                }
            }
            throw new DatabaseConnectionException(
                "儲存支出記錄失敗: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // 記錄錯誤但不拋出，避免覆蓋原始異常
                    System.err.println("關閉連線失敗: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<StatisticsPoint> findStatisticsByUserAndDate(User user, LocalDate date) {
        if (user == null || date == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT e.username, c.category_name, SUM(e.money) as total_amount " +
                     "FROM expenditure_records e " +
                     "JOIN expenditure_categories c ON e.id = c.expenditure_id " +
                     "WHERE e.username = ? AND e.date = ? " +
                     "GROUP BY e.username, c.category_name";
        
        return executeStatisticsQuery(date, sql, user.getUsername(), Date.valueOf(date));
    }

    @Override
    public List<StatisticsPoint> findStatisticsByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT e.username, c.category_name, SUM(e.money) as total_amount " +
                     "FROM expenditure_records e " +
                     "JOIN expenditure_categories c ON e.id = c.expenditure_id " +
                     "WHERE e.date = ? " +
                     "GROUP BY e.username, c.category_name";
        
        return executeStatisticsQuery(date, sql, Date.valueOf(date));
    }
    
    @Override
    public List<StatisticsPoint> findStatisticsByCategoryAndDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT c.category_name, SUM(e.money) as total_amount " +
                     "FROM expenditure_records e " +
                     "JOIN expenditure_categories c ON e.id = c.expenditure_id " +
                     "WHERE e.date = ? " +
                     "GROUP BY c.category_name";
        
        return executeStatisticsQueryByCategory(date, sql, Date.valueOf(date));
    }
    
    /**
     * 執行統計查詢並轉換為 StatisticsPoint 列表
     * 
     * @param queryDate 查詢日期，用於設置 StatisticsPoint 的時間
     * @param sql SQL 查詢語句
     * @param params SQL 參數
     */
    private List<StatisticsPoint> executeStatisticsQuery(
            LocalDate queryDate, String sql, Object... params) {
        
        List<StatisticsPoint> points = new ArrayList<>();
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 設定參數
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String categoryName = rs.getString("category_name");
                    int totalAmount = rs.getInt("total_amount");
                    
                    // 使用 UserIdentity 避免額外的資料庫查詢
                    UserIdentity userIdentity = UserIdentity.of(username);
                    
                    // 轉換分類名稱為 StatisticsCategory enum
                    // 如果分類名稱無法對應到 enum，則跳過該筆資料
                    try {
                        StatisticsCategory category = StatisticsCategory.valueOf(categoryName);
                        
                        LocalDateTime time = queryDate.atStartOfDay();
                        
                        StatisticsPoint point = new StatisticsPoint(
                            totalAmount, userIdentity, time, category
                        );
                        points.add(point);
                    } catch (IllegalArgumentException e) {
                        // 分類名稱不在 enum 中，忽略該筆資料
                        System.err.println("警告: 未知的統計分類 '" + categoryName + 
                                         "'，已略過 (使用者: " + username + ")");
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                "查詢統計資料失敗: " + e.getMessage(), e);
        }
        
        return points;
    }
    
    /**
     * 執行統計查詢並轉換為 StatisticsPoint 列表（僅按分類，不分使用者）
     * 
     * @param queryDate 查詢日期，用於設置 StatisticsPoint 的時間
     * @param sql SQL 查詢語句
     * @param params SQL 參數
     */
    private List<StatisticsPoint> executeStatisticsQueryByCategory(
            LocalDate queryDate, String sql, Object... params) {
        
        List<StatisticsPoint> points = new ArrayList<>();
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 設定參數
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String categoryName = rs.getString("category_name");
                    int totalAmount = rs.getInt("total_amount");
                    
                    // 轉換分類名稱為 StatisticsCategory enum
                    try {
                        StatisticsCategory category = StatisticsCategory.valueOf(categoryName);
                        
                        LocalDateTime time = queryDate.atStartOfDay();
                        
                        // userIdentity 設為 null 表示不分使用者的統計
                        StatisticsPoint point = new StatisticsPoint(
                            totalAmount, (UserIdentity) null, time, category
                        );
                        points.add(point);
                    } catch (IllegalArgumentException e) {
                        // 分類名稱不在 enum 中，忽略該筆資料
                        System.err.println("警告: 未知的統計分類 '" + categoryName + "'，已略過");
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                "查詢統計資料失敗: " + e.getMessage(), e);
        }
        
        return points;
    }
    
    @Override
    public PageResult<ExpenditureRecord> findRecentByUserWithCursor(User user, Cursor cursor, int limit) {
        if (user == null) {
            return new PageResult<>(new ArrayList<>());
        }
        
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100, got: " + limit);
        }
        
        // 查詢 limit + 1 筆資料以判斷是否還有下一頁
        int queryLimit = limit + 1;
        
        String sql;
        List<Object> params = new ArrayList<>();
        params.add(user.getUsername());
        
        if (cursor == null) {
            // 第一頁：不使用游標條件
            sql = "SELECT e.id, e.username, e.name, e.money, e.payment_method, e.date " +
                  "FROM expenditure_records e " +
                  "WHERE e.username = ? " +
                  "ORDER BY e.date DESC, e.id DESC " +
                  "LIMIT ?";
            params.add(queryLimit);
        } else {
            // 使用游標條件：(date < cursor_date) OR (date = cursor_date AND id < cursor_id)
            sql = "SELECT e.id, e.username, e.name, e.money, e.payment_method, e.date " +
                  "FROM expenditure_records e " +
                  "WHERE e.username = ? " +
                  "AND (e.date < ? OR (e.date = ? AND e.id < ?)) " +
                  "ORDER BY e.date DESC, e.id DESC " +
                  "LIMIT ?";
            params.add(Date.valueOf(cursor.getDate()));
            params.add(Date.valueOf(cursor.getDate()));
            params.add(cursor.getId());
            params.add(queryLimit);
        }
        
        // 使用修改後的方法，直接返回帶有 ID 的記錄
        List<RecordWithId> recordsWithIds = executeQueryWithCategoriesAndIds(sql, params.toArray());
        
        // 判斷是否有下一頁
        boolean hasMore = recordsWithIds.size() > limit;
        if (hasMore) {
            recordsWithIds = recordsWithIds.subList(0, limit); // 移除多查詢的那一筆
        }
        
        // 建立下一頁的游標
        Cursor nextCursor = null;
        if (hasMore && !recordsWithIds.isEmpty()) {
            RecordWithId lastRecord = recordsWithIds.get(recordsWithIds.size() - 1);
            nextCursor = new Cursor(lastRecord.record.getDate(), lastRecord.id);
        }
        
        // 提取記錄列表
        List<ExpenditureRecord> records = new ArrayList<>();
        for (RecordWithId rwi : recordsWithIds) {
            records.add(rwi.record);
        }
        
        return new PageResult<>(records, nextCursor, hasMore);
    }
    
    @Override
    public PageResult<ExpenditureRecord> findRecentByUserWithOffset(User user, int offset, int limit) {
        if (user == null) {
            return new PageResult<>(new ArrayList<>());
        }
        
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative, got: " + offset);
        }
        
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100, got: " + limit);
        }
        
        // 查詢 limit + 1 筆資料以判斷是否還有下一頁
        int queryLimit = limit + 1;
        
        String sql = "SELECT e.id, e.username, e.name, e.money, e.payment_method, e.date " +
                     "FROM expenditure_records e " +
                     "WHERE e.username = ? " +
                     "ORDER BY e.date DESC, e.id DESC " +
                     "LIMIT ? OFFSET ?";
        
        List<ExpenditureRecord> records = executeQueryWithCategoriesOptimized(
            sql, user.getUsername(), queryLimit, offset);
        
        // 判斷是否有下一頁
        boolean hasMore = records.size() > limit;
        if (hasMore) {
            records = records.subList(0, limit); // 移除多查詢的那一筆
        }
        
        // OFFSET-based 分頁也返回 cursor（用於一致的 API），但實際使用中不需要
        Cursor nextCursor = null;
        if (hasMore && !records.isEmpty()) {
            ExpenditureRecord lastRecord = records.get(records.size() - 1);
            Long lastRecordId = getRecordId(lastRecord);
            if (lastRecordId != null) {
                nextCursor = new Cursor(lastRecord.getDate(), lastRecordId);
            }
        }
        
        return new PageResult<>(records, nextCursor, hasMore);
    }
    
    /**
     * 執行查詢並優化載入關聯的分類資料（避免 N+1 問題）
     * 使用單一的 IN 查詢載入所有分類
     */
    /**
     * 執行查詢並批次載入分類（優化版，返回帶 ID 的記錄）
     * 用於分頁查詢以避免額外的 ID 查詢
     */
    private List<RecordWithId> executeQueryWithCategoriesAndIds(String sql, Object... params) {
        List<RecordWithId> recordsWithIds = new ArrayList<>();
        List<Long> expenditureIds = new ArrayList<>();
        Map<Long, ExpenditureRecord> recordMap = new HashMap<>();
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 設定參數
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            // 第一步：查詢主記錄
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String name = rs.getString("name");
                    int money = rs.getInt("money");
                    String paymentMethodStr = rs.getString("payment_method");
                    Date date = rs.getDate("date");
                    
                    PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
                    UserIdentity userIdentity = UserIdentity.of(username);
                    
                    // 先用空的分類列表建立記錄
                    ExpenditureRecord record = new ExpenditureRecord(
                        userIdentity, name, money, new ArrayList<>(), paymentMethod, date.toLocalDate()
                    );
                    
                    expenditureIds.add(id);
                    recordMap.put(id, record);
                }
            }
            
            // 第二步：批次查詢所有分類（避免 N+1）
            if (!expenditureIds.isEmpty()) {
                loadCategoriesInBatch(conn, expenditureIds, recordMap);
                
                // 按照原始查詢順序建立結果列表
                for (Long id : expenditureIds) {
                    ExpenditureRecord record = recordMap.get(id);
                    if (record != null) {
                        recordsWithIds.add(new RecordWithId(record, id));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                "查詢支出記錄失敗: " + e.getMessage(), e);
        }
        
        return recordsWithIds;
    }
    
    private List<ExpenditureRecord> executeQueryWithCategoriesOptimized(String sql, Object... params) {
        List<ExpenditureRecord> records = new ArrayList<>();
        List<Long> expenditureIds = new ArrayList<>();
        Map<Long, ExpenditureRecord> recordMap = new HashMap<>();
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 設定參數
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            // 第一步：查詢主記錄
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String name = rs.getString("name");
                    int money = rs.getInt("money");
                    String paymentMethodStr = rs.getString("payment_method");
                    Date date = rs.getDate("date");
                    
                    PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
                    UserIdentity userIdentity = UserIdentity.of(username);
                    
                    // 先用空的分類列表建立記錄
                    ExpenditureRecord record = new ExpenditureRecord(
                        userIdentity, name, money, new ArrayList<>(), paymentMethod, date.toLocalDate()
                    );
                    
                    records.add(record);
                    expenditureIds.add(id);
                    recordMap.put(id, record);
                }
            }
            
            // 第二步：批次查詢所有分類（避免 N+1）
            if (!expenditureIds.isEmpty()) {
                loadCategoriesInBatch(conn, expenditureIds, recordMap);
            }
            
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                "查詢支出記錄失敗: " + e.getMessage(), e);
        }
        
        return records;
    }
    
    /**
     * 批次載入分類資料（避免 N+1 問題）
     */
    private void loadCategoriesInBatch(Connection conn, List<Long> expenditureIds, 
            Map<Long, ExpenditureRecord> recordMap) throws SQLException {
        
        // 建立 IN 子句的佔位符
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < expenditureIds.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }
        
        String sql = "SELECT expenditure_id, category_name FROM expenditure_categories " +
                     "WHERE expenditure_id IN (" + inClause + ")";
        
        // 使用 Map 收集每個 expenditure_id 對應的分類
        Map<Long, List<String>> categoriesMap = new HashMap<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < expenditureIds.size(); i++) {
                pstmt.setLong(i + 1, expenditureIds.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long expenditureId = rs.getLong("expenditure_id");
                    String categoryName = rs.getString("category_name");
                    
                    categoriesMap.computeIfAbsent(expenditureId, k -> new ArrayList<>())
                                 .add(categoryName);
                }
            }
        }
        
        // 將分類設置到對應的記錄中（使用反射或重新建立記錄）
        // 因為 ExpenditureRecord 的分類是不可變的，我們需要重新建立記錄
        for (Map.Entry<Long, ExpenditureRecord> entry : recordMap.entrySet()) {
            Long id = entry.getKey();
            ExpenditureRecord oldRecord = entry.getValue();
            List<String> categories = categoriesMap.getOrDefault(id, new ArrayList<>());
            
            // 重新建立記錄（包含分類）
            ExpenditureRecord newRecord = new ExpenditureRecord(
                oldRecord.getUserIdentity(),
                oldRecord.getName(),
                oldRecord.getMoney(),
                categories,
                oldRecord.getPayway(),
                oldRecord.getDate()
            );
            
            recordMap.put(id, newRecord);
        }
    }
    
    /**
     * 取得記錄的資料庫 ID
     * 注意：ExpenditureRecord 目前沒有儲存 ID，需要從資料庫查詢
     */
    private Long getRecordId(ExpenditureRecord record) {
        String sql = "SELECT id FROM expenditure_records " +
                     "WHERE username = ? AND name = ? AND money = ? AND date = ? " +
                     "ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getUsername());
            pstmt.setString(2, record.getName());
            pstmt.setInt(3, record.getMoney());
            pstmt.setDate(4, Date.valueOf(record.getDate()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("警告: 無法取得記錄 ID: " + e.getMessage());
        }
        
        return null;
    }
}
