-- ========================================
-- Performance Test Data Generation Script
-- 效能測試資料生成腳本
-- 
-- 此腳本將生成大量測試資料用於 INDEX 效能比較
-- 目標：100,000+ Orders, 500,000+ OrderDetails, 1,000,000+ PerformanceTest 記錄
-- ========================================

USE northwind;

SET FOREIGN_KEY_CHECKS = 0;
SET AUTOCOMMIT = 0;

SELECT 'Starting Performance Data Generation...' AS Status;
SELECT 'This may take 2-5 minutes. Please wait...' AS Note;

-- ========================================
-- 第一部分：生成大量 Orders 資料
-- 目標：約 100,000 筆訂單
-- ========================================

SELECT '1/4: Generating Orders data (Round 1: 20,000 records)...' AS Progress;

-- 使用 CROSS JOIN 技巧快速生成大量資料
-- 從現有的 5 筆訂單複製並修改日期、ID 等
INSERT INTO Orders (CustomerID, EmployeeID, OrderDate, RequiredDate, ShippedDate, ShipVia, Freight, ShipName, ShipAddress, ShipCity, ShipPostalCode, ShipCountry)
SELECT 
    c.CustomerID,
    (e.num % 9) + 1 AS EmployeeID,  -- 循環使用 9 位員工
    DATE_ADD('2022-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num) DAY) AS OrderDate,
    DATE_ADD('2022-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + 30) DAY) AS RequiredDate,
    DATE_ADD('2022-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + (e.num % 15)) DAY) AS ShippedDate,
    ((t1.num + t2.num) % 3) + 1 AS ShipVia,  -- 循環使用 3 個運輸公司
    (10 + (t1.num * 1.5 + t2.num * 0.8 + t3.num * 0.3)) AS Freight,
    c.CompanyName AS ShipName,
    c.Address AS ShipAddress,
    c.City AS ShipCity,
    c.PostalCode AS ShipPostalCode,
    c.Country AS ShipCountry
FROM 
    Customers c
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6) e
LIMIT 20000;

COMMIT;

SELECT '1/4: Generating Orders data (Round 2: 20,000 records)...' AS Progress;

-- 第二批 20,000 筆
INSERT INTO Orders (CustomerID, EmployeeID, OrderDate, RequiredDate, ShippedDate, ShipVia, Freight, ShipName, ShipAddress, ShipCity, ShipPostalCode, ShipCountry)
SELECT 
    c.CustomerID,
    (e.num % 9) + 1 AS EmployeeID,
    DATE_ADD('2023-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num) DAY) AS OrderDate,
    DATE_ADD('2023-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + 30) DAY) AS RequiredDate,
    DATE_ADD('2023-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + (e.num % 15)) DAY) AS ShippedDate,
    ((t1.num + t2.num) % 3) + 1 AS ShipVia,
    (10 + (t1.num * 1.5 + t2.num * 0.8 + t3.num * 0.3)) AS Freight,
    c.CompanyName AS ShipName,
    c.Address AS ShipAddress,
    c.City AS ShipCity,
    c.PostalCode AS ShipPostalCode,
    c.Country AS ShipCountry
FROM 
    Customers c
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6) e
LIMIT 20000;

COMMIT;

SELECT '1/4: Generating Orders data (Round 3: 20,000 records)...' AS Progress;

-- 第三批 20,000 筆
INSERT INTO Orders (CustomerID, EmployeeID, OrderDate, RequiredDate, ShippedDate, ShipVia, Freight, ShipName, ShipAddress, ShipCity, ShipPostalCode, ShipCountry)
SELECT 
    c.CustomerID,
    (e.num % 9) + 1 AS EmployeeID,
    DATE_ADD('2024-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num) DAY) AS OrderDate,
    DATE_ADD('2024-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + 30) DAY) AS RequiredDate,
    DATE_ADD('2024-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + (e.num % 15)) DAY) AS ShippedDate,
    ((t1.num + t2.num) % 3) + 1 AS ShipVia,
    (10 + (t1.num * 1.5 + t2.num * 0.8 + t3.num * 0.3)) AS Freight,
    c.CompanyName AS ShipName,
    c.Address AS ShipAddress,
    c.City AS ShipCity,
    c.PostalCode AS ShipPostalCode,
    c.Country AS ShipCountry
FROM 
    Customers c
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6) e
LIMIT 20000;

COMMIT;

SELECT '1/4: Generating Orders data (Round 4: 20,000 records)...' AS Progress;

-- 第四批 20,000 筆
INSERT INTO Orders (CustomerID, EmployeeID, OrderDate, RequiredDate, ShippedDate, ShipVia, Freight, ShipName, ShipAddress, ShipCity, ShipPostalCode, ShipCountry)
SELECT 
    c.CustomerID,
    (e.num % 9) + 1 AS EmployeeID,
    DATE_ADD('2025-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num) DAY) AS OrderDate,
    DATE_ADD('2025-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + 30) DAY) AS RequiredDate,
    DATE_ADD('2025-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + (e.num % 15)) DAY) AS ShippedDate,
    ((t1.num + t2.num) % 3) + 1 AS ShipVia,
    (10 + (t1.num * 1.5 + t2.num * 0.8 + t3.num * 0.3)) AS Freight,
    c.CompanyName AS ShipName,
    c.Address AS ShipAddress,
    c.City AS ShipCity,
    c.PostalCode AS ShipPostalCode,
    c.Country AS ShipCountry
FROM 
    Customers c
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6) e
LIMIT 20000;

COMMIT;

SELECT '1/4: Generating Orders data (Round 5: 20,000 records)...' AS Progress;

-- 第五批 20,000 筆
INSERT INTO Orders (CustomerID, EmployeeID, OrderDate, RequiredDate, ShippedDate, ShipVia, Freight, ShipName, ShipAddress, ShipCity, ShipPostalCode, ShipCountry)
SELECT 
    c.CustomerID,
    (e.num % 9) + 1 AS EmployeeID,
    DATE_ADD('2026-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num) DAY) AS OrderDate,
    DATE_ADD('2026-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + 30) DAY) AS RequiredDate,
    DATE_ADD('2026-01-01', INTERVAL (t1.num * 100 + t2.num * 10 + t3.num + (e.num % 15)) DAY) AS ShippedDate,
    ((t1.num + t2.num) % 3) + 1 AS ShipVia,
    (10 + (t1.num * 1.5 + t2.num * 0.8 + t3.num * 0.3)) AS Freight,
    c.CompanyName AS ShipName,
    c.Address AS ShipAddress,
    c.City AS ShipCity,
    c.PostalCode AS ShipPostalCode,
    c.Country AS ShipCountry
FROM 
    Customers c
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6) e
LIMIT 20000;

COMMIT;

SELECT CONCAT('Orders generation complete: ', COUNT(*), ' records') AS Progress FROM Orders;

-- ========================================
-- 第二部分：生成 OrderDetails 資料
-- 每個訂單平均有 3-7 個產品項目
-- ========================================

SELECT '2/4: Generating OrderDetails data (this may take longer)...' AS Progress;

-- 為每個訂單生成訂單明細（使用批次處理避免記憶體問題）
-- 使用 stored procedure 更有效率

DELIMITER //

CREATE PROCEDURE IF NOT EXISTS GenerateOrderDetails()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_orderid INT;
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_items INT;
    DECLARE v_product INT;
    DECLARE cur CURSOR FOR SELECT OrderID FROM Orders WHERE OrderID > 10252 ORDER BY OrderID;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_orderid;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 每個訂單隨機 3-7 個項目
        SET v_items = 3 + (v_orderid % 5);
        
        -- 為這個訂單插入多個產品項目
        SET v_product = 1;
        WHILE v_product <= v_items DO
            INSERT INTO OrderDetails (OrderID, ProductID, UnitPrice, Quantity, Discount)
            SELECT 
                v_orderid,
                ((v_orderid * v_product + v_count) % 77) + 1 AS ProductID,
                p.UnitPrice,
                ((v_orderid + v_product) % 50) + 1 AS Quantity,
                CASE 
                    WHEN (v_orderid + v_product) % 10 = 0 THEN 0.10
                    WHEN (v_orderid + v_product) % 20 = 0 THEN 0.15
                    WHEN (v_orderid + v_product) % 30 = 0 THEN 0.20
                    ELSE 0
                END AS Discount
            FROM Products p
            WHERE p.ProductID = ((v_orderid * v_product + v_count) % 77) + 1
            ON DUPLICATE KEY UPDATE Quantity = Quantity + 1;
            
            SET v_product = v_product + 1;
        END WHILE;

        SET v_count = v_count + 1;
        
        -- 每 10000 筆提交一次
        IF v_count % 10000 = 0 THEN
            COMMIT;
            SELECT CONCAT('  OrderDetails: Processed ', v_count, ' orders...') AS Status;
        END IF;
    END LOOP;

    CLOSE cur;
    COMMIT;
    SELECT CONCAT('OrderDetails generation complete: ', COUNT(*), ' records') AS Final FROM OrderDetails;
END //

DELIMITER ;

-- 執行 stored procedure
CALL GenerateOrderDetails();

-- 清理 stored procedure
DROP PROCEDURE IF EXISTS GenerateOrderDetails;

-- ========================================
-- 第三部分：生成 PerformanceTest 表的大量測試資料
-- 目標：1,000,000 筆記錄用於 INDEX 效能比較
-- ========================================

SELECT '3/4: Generating PerformanceTest data (1,000,000 records in batches)...' AS Progress;

-- 批次 1: 200,000 筆
INSERT INTO PerformanceTest (indexed_col, non_indexed_col, varchar_col, text_col)
SELECT 
    t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num AS indexed_col,
    (t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) * 7 % 1000000 AS non_indexed_col,
    CONCAT('Test_Data_', t1.num, '_', t2.num, '_', t3.num, '_', t4.num, '_', t5.num) AS varchar_col,
    CONCAT('This is sample text data for performance testing. Row: ', 
           t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) AS text_col
FROM 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t4
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1) t5
LIMIT 200000;

COMMIT;
SELECT '  Batch 1/5 complete (200,000 records)' AS Progress;

-- 批次 2: 200,000 筆
INSERT INTO PerformanceTest (indexed_col, non_indexed_col, varchar_col, text_col)
SELECT 
    200000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num AS indexed_col,
    (200000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) * 7 % 1000000 AS non_indexed_col,
    CONCAT('Test_Data_', 20 + t1.num, '_', t2.num, '_', t3.num, '_', t4.num, '_', t5.num) AS varchar_col,
    CONCAT('This is sample text data for performance testing. Row: ', 
           200000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) AS text_col
FROM 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t4
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1) t5
LIMIT 200000;

COMMIT;
SELECT '  Batch 2/5 complete (400,000 total records)' AS Progress;

-- 批次 3: 200,000 筆
INSERT INTO PerformanceTest (indexed_col, non_indexed_col, varchar_col, text_col)
SELECT 
    400000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num AS indexed_col,
    (400000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) * 7 % 1000000 AS non_indexed_col,
    CONCAT('Test_Data_', 40 + t1.num, '_', t2.num, '_', t3.num, '_', t4.num, '_', t5.num) AS varchar_col,
    CONCAT('This is sample text data for performance testing. Row: ', 
           400000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) AS text_col
FROM 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t4
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1) t5
LIMIT 200000;

COMMIT;
SELECT '  Batch 3/5 complete (600,000 total records)' AS Progress;

-- 批次 4: 200,000 筆
INSERT INTO PerformanceTest (indexed_col, non_indexed_col, varchar_col, text_col)
SELECT 
    600000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num AS indexed_col,
    (600000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) * 7 % 1000000 AS non_indexed_col,
    CONCAT('Test_Data_', 60 + t1.num, '_', t2.num, '_', t3.num, '_', t4.num, '_', t5.num) AS varchar_col,
    CONCAT('This is sample text data for performance testing. Row: ', 
           600000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) AS text_col
FROM 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t4
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1) t5
LIMIT 200000;

COMMIT;
SELECT '  Batch 4/5 complete (800,000 total records)' AS Progress;

-- 批次 5: 200,000 筆
INSERT INTO PerformanceTest (indexed_col, non_indexed_col, varchar_col, text_col)
SELECT 
    800000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num AS indexed_col,
    (800000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) * 7 % 1000000 AS non_indexed_col,
    CONCAT('Test_Data_', 80 + t1.num, '_', t2.num, '_', t3.num, '_', t4.num, '_', t5.num) AS varchar_col,
    CONCAT('This is sample text data for performance testing. Row: ', 
           800000 + t1.num * 10000 + t2.num * 1000 + t3.num * 100 + t4.num * 10 + t5.num) AS text_col
FROM 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t3
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
     UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t4
CROSS JOIN 
    (SELECT 0 AS num UNION SELECT 1) t5
LIMIT 200000;

COMMIT;
SELECT '  Batch 5/5 complete (1,000,000 total records)' AS Progress;

SELECT CONCAT('PerformanceTest generation complete: ', COUNT(*), ' records') AS Progress FROM PerformanceTest;

-- ========================================
-- 第四部分：優化和統計資訊更新
-- ========================================

SELECT '4/4: Optimizing tables and updating statistics...' AS Progress;

-- 分析和優化表以提升查詢效能
ANALYZE TABLE Orders;
ANALYZE TABLE OrderDetails;
ANALYZE TABLE PerformanceTest;
ANALYZE TABLE Products;
ANALYZE TABLE Customers;
ANALYZE TABLE Employees;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
SET AUTOCOMMIT = 1;

-- ========================================
-- 完成報告
-- ========================================

SELECT '========================================' AS '';
SELECT 'Performance Data Generation Complete!' AS Status;
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT 'Data Summary:' AS '';
SELECT CONCAT('  Categories: ', COUNT(*)) AS Info FROM Categories
UNION ALL
SELECT CONCAT('  Suppliers: ', COUNT(*)) FROM Suppliers
UNION ALL
SELECT CONCAT('  Products: ', COUNT(*)) FROM Products
UNION ALL
SELECT CONCAT('  Customers: ', COUNT(*)) FROM Customers
UNION ALL
SELECT CONCAT('  Employees: ', COUNT(*)) FROM Employees
UNION ALL
SELECT CONCAT('  Orders: ', COUNT(*)) FROM Orders
UNION ALL
SELECT CONCAT('  OrderDetails: ', COUNT(*)) FROM OrderDetails
UNION ALL
SELECT CONCAT('  PerformanceTest: ', COUNT(*)) FROM PerformanceTest;

SELECT '' AS '';
SELECT 'You can now practice:' AS '';
SELECT '  ✓ CRUD operations on all tables' AS '';
SELECT '  ✓ Complex JOINs with large datasets' AS '';
SELECT '  ✓ WHERE clauses with various conditions' AS '';
SELECT '  ✓ GROUP BY and aggregation functions' AS '';
SELECT '  ✓ INDEX performance comparisons with 100K+ records' AS '';
SELECT '========================================' AS '';
