-- ========================================
-- INDEX Performance Testing Examples
-- 索引效能測試範例
-- 
-- 此腳本提供各種 INDEX 效能比較範例
-- 使用 EXPLAIN 分析查詢計劃，比較有索引與無索引的效能差異
-- ========================================

USE northwind;

SELECT '========================================' AS '';
SELECT 'INDEX Performance Testing Guide' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT 'This script demonstrates INDEX performance differences.' AS '';
SELECT 'Use EXPLAIN to see query execution plans.' AS '';
SELECT 'Compare execution times with and without indexes.' AS '';
SELECT '' AS '';

-- ========================================
-- 測試 1: 基本索引 vs 全表掃描
-- ========================================

SELECT '========================================' AS '';
SELECT 'Test 1: Index vs Full Table Scan' AS '';
SELECT '========================================' AS '';

-- 1a. 查詢無索引的欄位（全表掃描）
SELECT '1a. Query on non-indexed column (Full Table Scan):' AS Test;
EXPLAIN 
SELECT * FROM PerformanceTest WHERE non_indexed_col = 123456;

-- 注意：type = ALL 表示全表掃描，rows 會顯示掃描的行數

-- 1b. 查詢有索引的欄位（使用索引）
SELECT '1b. Query on indexed column (Using Index):' AS Test;
EXPLAIN 
SELECT * FROM PerformanceTest WHERE indexed_col = 123456;

-- 注意：type = ref 或 const，使用了索引，rows 會大大減少

SELECT '' AS '';
SELECT '比較結果：' AS '';
SELECT '  - 無索引：type=ALL, 掃描全表 (1,000,000 rows)' AS '';
SELECT '  - 有索引：type=ref, 只掃描必要行 (~1 row)' AS '';
SELECT '  - 效能差異：可達 1000 倍以上！' AS '';

-- ========================================
-- 測試 2: 實際執行時間比較
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 2: Actual Execution Time Comparison' AS '';
SELECT '========================================' AS '';

-- 啟用 profiling 以測量執行時間
SET profiling = 1;

-- 2a. 無索引查詢
SELECT COUNT(*) FROM PerformanceTest WHERE non_indexed_col = 123456;

-- 2b. 有索引查詢
SELECT COUNT(*) FROM PerformanceTest WHERE indexed_col = 123456;

-- 2c. 顯示執行時間
SELECT 'Execution time comparison:' AS '';
SHOW PROFILES;

SELECT '' AS '';
SELECT '提示：Duration 欄位顯示執行時間（秒）' AS '';
SELECT '有索引的查詢應該快很多！' AS '';

-- ========================================
-- 測試 3: 為無索引欄位添加索引
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 3: Adding Index to non-indexed Column' AS '';
SELECT '========================================' AS '';

-- 3a. 添加索引前的查詢計劃
SELECT '3a. Before adding index:' AS Test;
EXPLAIN 
SELECT * FROM PerformanceTest WHERE non_indexed_col BETWEEN 100000 AND 100100;

-- 3b. 創建索引
SELECT '3b. Creating index on non_indexed_col...' AS Status;
CREATE INDEX idx_non_indexed_col ON PerformanceTest(non_indexed_col);

-- 3c. 添加索引後的查詢計劃
SELECT '3c. After adding index:' AS Test;
EXPLAIN 
SELECT * FROM PerformanceTest WHERE non_indexed_col BETWEEN 100000 AND 100100;

SELECT '' AS '';
SELECT '注意 type 和 rows 的變化！' AS '';

-- ========================================
-- 測試 4: 複合索引 (Composite Index)
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 4: Composite Index' AS '';
SELECT '========================================' AS '';

-- 4a. Orders 表的日期範圍查詢（已有 OrderDate 索引）
SELECT '4a. Query with single column index:' AS Test;
EXPLAIN 
SELECT * FROM Orders 
WHERE OrderDate >= '2024-01-01' AND OrderDate < '2024-02-01';

-- 4b. 添加複合索引：日期 + 客戶
SELECT '4b. Creating composite index (OrderDate, CustomerID)...' AS Status;
CREATE INDEX idx_orders_date_customer ON Orders(OrderDate, CustomerID);

-- 4c. 使用複合索引的查詢
SELECT '4c. Query using composite index:' AS Test;
EXPLAIN 
SELECT * FROM Orders 
WHERE OrderDate >= '2024-01-01' 
  AND OrderDate < '2024-02-01' 
  AND CustomerID = 'ALFKI';

SELECT '' AS '';
SELECT '複合索引可以同時用於多個查詢條件' AS '';

-- ========================================
-- 測試 5: 覆蓋索引 (Covering Index)
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 5: Covering Index' AS '';
SELECT '========================================' AS '';

-- 5a. 需要讀取表數據的查詢
SELECT '5a. Query requiring table lookup:' AS Test;
EXPLAIN 
SELECT OrderID, OrderDate, CustomerID, Freight
FROM Orders 
WHERE OrderDate >= '2024-01-01';

-- 5b. 創建覆蓋索引（包含所有查詢欄位）
SELECT '5b. Creating covering index...' AS Status;
CREATE INDEX idx_orders_covering ON Orders(OrderDate, OrderID, CustomerID, Freight);

-- 5c. 使用覆蓋索引的查詢
SELECT '5c. Query using covering index:' AS Test;
EXPLAIN 
SELECT OrderID, OrderDate, CustomerID, Freight
FROM Orders 
WHERE OrderDate >= '2024-01-01';

SELECT '' AS '';
SELECT '注意 Extra 欄位：Using index 表示只需讀取索引，不用訪問表' AS '';

-- ========================================
-- 測試 6: 索引選擇性 (Index Selectivity)
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 6: Index Selectivity' AS '';
SELECT '========================================' AS '';

-- 6a. 高選擇性欄位（值分散，適合索引）
SELECT '6a. High selectivity column (OrderID - unique):' AS Test;
SELECT COUNT(DISTINCT OrderID) AS DistinctValues, 
       COUNT(*) AS TotalRows,
       COUNT(DISTINCT OrderID) / COUNT(*) AS Selectivity
FROM Orders;

-- 6b. 低選擇性欄位（值集中，索引效果差）
SELECT '6b. Low selectivity column (ShipVia - only 3 values):' AS Test;
SELECT COUNT(DISTINCT ShipVia) AS DistinctValues, 
       COUNT(*) AS TotalRows,
       COUNT(DISTINCT ShipVia) / COUNT(*) AS Selectivity
FROM Orders;

SELECT '' AS '';
SELECT '選擇性建議：' AS '';
SELECT '  - 高選擇性 (> 0.8)：適合建立索引' AS '';
SELECT '  - 中選擇性 (0.1-0.8)：視查詢頻率決定' AS '';
SELECT '  - 低選擇性 (< 0.1)：不建議建立索引' AS '';

-- ========================================
-- 測試 7: JOIN 查詢的索引影響
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 7: Index Impact on JOIN' AS '';
SELECT '========================================' AS '';

-- 7a. 多表 JOIN（外鍵已有索引）
SELECT '7a. JOIN query with foreign key indexes:' AS Test;
EXPLAIN 
SELECT o.OrderID, c.CompanyName, od.ProductID, od.Quantity
FROM Orders o
INNER JOIN Customers c ON o.CustomerID = c.CustomerID
INNER JOIN OrderDetails od ON o.OrderID = od.OrderID
WHERE o.OrderDate >= '2024-01-01'
LIMIT 100;

SELECT '' AS '';
SELECT '注意每個表的 type 和 key，外鍵索引對 JOIN 性能至關重要' AS '';

-- ========================================
-- 測試 8: ORDER BY 和索引
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 8: INDEX and ORDER BY' AS '';
SELECT '========================================' AS '';

-- 8a. 使用索引排序
SELECT '8a. ORDER BY indexed column:' AS Test;
EXPLAIN 
SELECT * FROM Orders 
ORDER BY OrderDate DESC 
LIMIT 100;

-- 8b. 不使用索引排序（需要 filesort）
SELECT '8b. ORDER BY non-indexed column:' AS Test;
EXPLAIN 
SELECT * FROM Orders 
ORDER BY Freight DESC 
LIMIT 100;

SELECT '' AS '';
SELECT '注意 Extra 欄位：' AS '';
SELECT '  - 無 filesort：索引已排序，直接使用' AS '';
SELECT '  - Using filesort：需額外排序操作，較慢' AS '';

-- ========================================
-- 測試 9: VARCHAR 索引 vs INT 索引
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 9: VARCHAR Index vs INT Index' AS '';
SELECT '========================================' AS '';

SET profiling = 1;

-- 9a. VARCHAR 欄位查詢（CustomerID 是 VARCHAR(5)）
SELECT COUNT(*) FROM Orders WHERE CustomerID = 'ALFKI';

-- 9b. INT 欄位查詢（EmployeeID 是 INT）
SELECT COUNT(*) FROM Orders WHERE EmployeeID = 5;

SELECT 'Performance comparison (VARCHAR vs INT):' AS '';
SHOW PROFILES;

SELECT '' AS '';
SELECT 'INT 索引通常比 VARCHAR 索引更快' AS '';

-- ========================================
-- 測試 10: 索引維護成本演示
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Test 10: Index Maintenance Cost' AS '';
SELECT '========================================' AS '';

SELECT '10a. Checking index size:' AS Test;
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    ROUND(STAT_VALUE * @@innodb_page_size / 1024 / 1024, 2) AS SizeMB
FROM mysql.innodb_index_stats
WHERE TABLE_NAME IN ('Orders', 'OrderDetails', 'PerformanceTest')
  AND DATABASE_NAME = 'northwind'
  AND STAT_NAME = 'size'
ORDER BY SizeMB DESC;

SELECT '' AS '';
SELECT '提示：索引會佔用磁碟空間，INSERT/UPDATE/DELETE 時也需要維護' AS '';
SELECT '不要過度建立索引！' AS '';

-- ========================================
-- 練習題
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Practice Exercises:' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT '1. 在 Products 表的 UnitPrice 欄位上創建索引' AS '';
SELECT '   然後查詢價格在 20-50 之間的產品，比較效能' AS '';
SELECT '' AS '';
SELECT '2. 創建 (CategoryID, UnitPrice) 複合索引' AS '';
SELECT '   測試同時按類別和價格查詢的效能' AS '';
SELECT '' AS '';
SELECT '3. 使用 EXPLAIN ANALYZE測試複雜 JOIN 查詢' AS '';
SELECT '   觀察各個表的訪問方式和行數' AS '';
SELECT '' AS '';
SELECT '4. 嘗試刪除某個索引，觀察查詢計劃的變化' AS '';
SELECT '' AS '';
SELECT '5. 在 PerformanceTest 的 varchar_col 上創建索引' AS '';
SELECT '   比較 LIKE 查詢的效能差異' AS '';

-- ========================================
-- 清理（可選）
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Cleanup (Optional):' AS '';
SELECT '========================================' AS '';
SELECT 'To remove test indexes created in this script:' AS '';
SELECT '' AS '';
SELECT '  DROP INDEX idx_non_indexed_col ON PerformanceTest;' AS '';
SELECT '  DROP INDEX idx_orders_date_customer ON Orders;' AS '';
SELECT '  DROP INDEX idx_orders_covering ON Orders;' AS '';
SELECT '' AS '';

SET profiling = 0;

SELECT '========================================' AS '';
SELECT 'INDEX Testing Complete!' AS '';
SELECT '實驗完畢！請繼續練習各種索引場景' AS '';
SELECT '========================================' AS '';
