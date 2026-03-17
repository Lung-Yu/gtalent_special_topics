****# SQL 查詢效能優化實戰教學 ⚡

> 📘 **教學目標：** 透過實際操作理解 SQL 查詢效能的關鍵因素，學會使用 EXPLAIN 分析查詢計畫，並掌握常見的優化技巧

> ⚠️ **重要提醒：** 本教學會產生大量測試資料，建議在測試環境執行。完成後記得清理測試資料。

---

## 📋 目錄

1. [環境準備與基礎概念](#1-環境準備與基礎概念)
2. [索引的威力 - Before & After](#2-索引的威力---before--after)
3. [JOIN 優化技巧](#3-join-優化技巧)
4. [子查詢 vs JOIN 效能比較](#4-子查詢-vs-join-效能比較)
5. [WHERE 條件順序與索引使用](#5-where-條件順序與索引使用)
6. [複合索引的使用策略](#6-複合索引的使用策略)
7. [聚合查詢優化](#7-聚合查詢優化)
8. [LIMIT 與分頁查詢優化](#8-limit-與分頁查詢優化)
9. [避免全表掃描的技巧](#9-避免全表掃描的技巧)
10. [實戰案例：優化慢查詢](#10-實戰案例優化慢查詢)

---

## 1. 環境準備與基礎概念

### 1.1 理解 EXPLAIN 輸出

EXPLAIN 是分析查詢效能的最重要工具，它顯示 MySQL 如何執行你的查詢。

**關鍵欄位說明：**

| 欄位 | 說明 | 重要性 |
|------|------|--------|
| **type** | 訪問類型 | ⭐⭐⭐⭐⭐ 最重要！|
| **possible_keys** | 可能使用的索引 | ⭐⭐⭐ |
| **key** | 實際使用的索引 | ⭐⭐⭐⭐⭐ |
| **rows** | 預估掃描的行數 | ⭐⭐⭐⭐ |
| **Extra** | 額外資訊 | ⭐⭐⭐⭐ |

**type 欄位的效能排序（從最好到最差）：**

```
system > const > eq_ref > ref > range > index > ALL

✅ 好的：const, eq_ref, ref, range
⚠️ 注意：index
❌ 避免：ALL（全表掃描）
```

### 1.2 建立測試環境

```sql
-- 連接到資料庫
USE northwind;

-- 檢查當前資料量
SELECT 
    'Orders' AS TableName, 
    COUNT(*) AS RowCount,
    ROUND(SUM(LENGTH(OrderID) + LENGTH(CustomerID) + LENGTH(EmployeeID)) / 1024, 2) AS ApproxSizeKB
FROM Orders
UNION ALL
SELECT 'OrderDetails', COUNT(*), 
    ROUND(SUM(LENGTH(OrderID) + LENGTH(ProductID)) / 1024, 2)
FROM OrderDetails
UNION ALL
SELECT 'Products', COUNT(*),
    ROUND(SUM(LENGTH(ProductID) + LENGTH(ProductName)) / 1024, 2)
FROM Products;
```

**預期輸出：** 看到各表的資料量

---

## 2. 索引的威力 - Before & After

### 2.1 建立大量測試資料

首先，我們建立一個新的測試表，用來展示索引的效果。

```sql
-- 步驟 1: 建立測試表
DROP TABLE IF EXISTS test_customers;

CREATE TABLE test_customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    city VARCHAR(50),
    country VARCHAR(50),
    registration_date DATE,
    total_spent DECIMAL(10,2),
    INDEX idx_primary (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

```sql
-- 步驟 2: 產生 100,000 筆測試資料
-- 注意：這需要幾分鐘，請耐心等待

DROP PROCEDURE IF EXISTS generate_test_customers;

DELIMITER //
CREATE PROCEDURE generate_test_customers(IN num_records INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE first_names VARCHAR(500) DEFAULT 'James,Mary,John,Patricia,Robert,Jennifer,Michael,Linda,William,Elizabeth,David,Barbara,Richard,Susan,Joseph,Jessica,Thomas,Sarah,Charles,Karen,Christopher,Nancy,Daniel,Lisa,Matthew,Betty,Anthony,Margaret,Mark,Sandra,Donald,Ashley,Steven,Kimberly,Paul,Emily,Andrew,Donna,Joshua,Michelle';
    DECLARE last_names VARCHAR(500) DEFAULT 'Smith,Johnson,Williams,Brown,Jones,Garcia,Miller,Davis,Rodriguez,Martinez,Hernandez,Lopez,Gonzalez,Wilson,Anderson,Thomas,Taylor,Moore,Jackson,Martin,Lee,Perez,Thompson,White,Harris,Sanchez,Clark,Ramirez,Lewis,Robinson,Walker,Young,Allen,King,Wright,Scott,Torres,Nguyen,Hill,Flores,Green,Adams,Nelson,Baker,Hall,Rivera,Campbell,Mitchell,Carter';
    DECLARE cities VARCHAR(500) DEFAULT 'New York,Los Angeles,Chicago,Houston,Phoenix,Philadelphia,San Antonio,San Diego,Dallas,San Jose,Austin,Jacksonville,Fort Worth,Columbus,Charlotte,San Francisco,Indianapolis,Seattle,Denver,Washington,Boston,Nashville,Detroit,Portland,Memphis,Oklahoma City,Las Vegas,Louisville,Baltimore,Milwaukee';
    DECLARE countries VARCHAR(300) DEFAULT 'USA,Canada,Mexico,UK,Germany,France,Spain,Italy,Australia,Japan,China,Brazil,India,South Korea,Netherlands,Sweden,Norway,Denmark,Finland,Switzerland';
    
    DECLARE fn VARCHAR(50);
    DECLARE ln VARCHAR(50);
    DECLARE ct VARCHAR(50);
    DECLARE co VARCHAR(50);
    
    WHILE i <= num_records DO
        SET fn = SUBSTRING_INDEX(SUBSTRING_INDEX(first_names, ',', FLOOR(1 + RAND() * 40)), ',', -1);
        SET ln = SUBSTRING_INDEX(SUBSTRING_INDEX(last_names, ',', FLOOR(1 + RAND() * 50)), ',', -1);
        SET ct = SUBSTRING_INDEX(SUBSTRING_INDEX(cities, ',', FLOOR(1 + RAND() * 30)), ',', -1);
        SET co = SUBSTRING_INDEX(SUBSTRING_INDEX(countries, ',', FLOOR(1 + RAND() * 20)), ',', -1);
        
        INSERT INTO test_customers (
            first_name, last_name, email, city, country, 
            registration_date, total_spent
        ) VALUES (
            fn,
            ln,
            CONCAT(LOWER(fn), '.', LOWER(ln), i, '@example.com'),
            ct,
            co,
            DATE_ADD('2020-01-01', INTERVAL FLOOR(RAND() * 2000) DAY),
            ROUND(RAND() * 10000, 2)
        );
        
        SET i = i + 1;
        
        IF i MOD 10000 = 0 THEN
            SELECT CONCAT('已產生 ', i, ' 筆資料...') AS Progress;
        END IF;
    END WHILE;
    
    SELECT CONCAT('完成！共產生 ', num_records, ' 筆測試資料') AS Result;
END //
DELIMITER ;

-- 執行：產生 100,000 筆資料
CALL generate_test_customers(100000);
```

**預期時間：** 2-5 分鐘（依機器效能而定）

### 2.2 查詢效能測試 - 無索引

```sql
-- 步驟 3: 測試沒有索引時的查詢效能

-- 📊 測試 1: 按國家查詢（無索引）
EXPLAIN SELECT * FROM test_customers WHERE country = 'USA';

-- 🔍 觀察 EXPLAIN 結果：
-- type: ALL (全表掃描，最差)
-- rows: ~100000 (需要掃描所有行)
-- Extra: Using where

-- 實際執行並測量時間
SET profiling = 1;

SELECT COUNT(*), AVG(total_spent) 
FROM test_customers 
WHERE country = 'USA';

SELECT COUNT(*), AVG(total_spent) 
FROM test_customers 
WHERE country = 'Germany';

SELECT COUNT(*), AVG(total_spent) 
FROM test_customers 
WHERE country = 'Japan';

-- 查看執行時間
SHOW PROFILES;

-- 📝 記錄你的結果：
-- Query 1 (USA): _______ seconds
-- Query 2 (Germany): _______ seconds  
-- Query 3 (Japan): _______ seconds
```

### 2.3 建立索引並重新測試

```sql
-- 步驟 4: 為 country 欄位建立索引

-- 測量建立索引的時間
SELECT NOW() AS start_time;

CREATE INDEX idx_country ON test_customers(country);

SELECT NOW() AS end_time;

-- 查看索引資訊
SHOW INDEX FROM test_customers;
```

```sql
-- 步驟 5: 使用索引後的查詢效能

-- 📊 測試 2: 按國家查詢（有索引）
EXPLAIN SELECT * FROM test_customers WHERE country = 'USA';

-- 🔍 觀察 EXPLAIN 結果變化：
-- type: ref (使用索引，好！)
-- possible_keys: idx_country
-- key: idx_country (確實使用了索引)
-- rows: ~5000 (大幅減少！)
-- Extra: Using index condition

-- 實際執行並測量時間
SET profiling = 1;

SELECT COUNT(*), AVG(total_spent) 
FROM test_customers 
WHERE country = 'USA';

SELECT COUNT(*), AVG(total_spent) 
FROM test_customers 
WHERE country = 'Germany';

SELECT COUNT(*), AVG(total_spent) 
FROM test_customers 
WHERE country = 'Japan';

SHOW PROFILES;

-- 📝 比較結果：
-- Query 1 (USA): _______ seconds (改善: ___%)
-- Query 2 (Germany): _______ seconds (改善: ___%)
-- Query 3 (Japan): _______ seconds (改善: ___%)
```

### 2.4 效能對比總結

```sql
-- 步驟 6: 分析索引的空間成本

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    ROUND(STAT_VALUE * @@innodb_page_size / 1024 / 1024, 2) AS IndexSizeMB
FROM mysql.innodb_index_stats
WHERE TABLE_NAME = 'test_customers'
    AND DATABASE_NAME = 'northwind'
    AND STAT_NAME = 'size';
```

**💡 學習重點：**
1. ✅ 索引大幅提升查詢速度（通常 10-100 倍）
2. ✅ type 從 ALL 變為 ref
3. ✅ 掃描行數大幅減少
4. ⚠️ 索引需要額外的儲存空間
5. ⚠️ 索引會略微降低 INSERT/UPDATE 速度

---

## 3. JOIN 優化技巧

### 3.1 建立測試資料

```sql
-- 步驟 1: 建立訂單測試表
DROP TABLE IF EXISTS test_orders;
DROP TABLE IF EXISTS test_order_items;

CREATE TABLE test_orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_date DATE NOT NULL,
    total_amount DECIMAL(10,2),
    status VARCHAR(20)
) ENGINE=InnoDB;

CREATE TABLE test_order_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_name VARCHAR(100),
    quantity INT,
    price DECIMAL(10,2)
) ENGINE=InnoDB;
```

```sql
-- 步驟 2: 產生訂單測試資料
DROP PROCEDURE IF EXISTS generate_test_orders;

DELIMITER //
CREATE PROCEDURE generate_test_orders(IN num_orders INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE j INT;
    DECLARE num_items INT;
    DECLARE current_order_id INT;
    
    WHILE i <= num_orders DO
        INSERT INTO test_orders (customer_id, order_date, total_amount, status)
        VALUES (
            FLOOR(1 + RAND() * 100000),
            DATE_ADD('2024-01-01', INTERVAL FLOOR(RAND() * 400) DAY),
            0,
            ELT(FLOOR(1 + RAND() * 4), 'pending', 'processing', 'completed', 'cancelled')
        );
        
        SET current_order_id = LAST_INSERT_ID();
        SET num_items = FLOOR(1 + RAND() * 10);
        SET j = 1;
        
        WHILE j <= num_items DO
            INSERT INTO test_order_items (order_id, product_name, quantity, price)
            VALUES (
                current_order_id,
                CONCAT('Product-', FLOOR(1 + RAND() * 1000)),
                FLOOR(1 + RAND() * 20),
                ROUND(10 + RAND() * 500, 2)
            );
            SET j = j + 1;
        END WHILE;
        
        SET i = i + 1;
        
        IF i MOD 5000 = 0 THEN
            SELECT CONCAT('已產生 ', i, ' 筆訂單...') AS Progress;
        END IF;
    END WHILE;
    
    UPDATE test_orders o
    SET total_amount = (
        SELECT SUM(quantity * price)
        FROM test_order_items
        WHERE order_id = o.order_id
    );
    
    SELECT CONCAT('完成！產生 ', num_orders, ' 筆訂單') AS Result;
END //
DELIMITER ;

-- 產生 50,000 筆訂單（約 30 萬筆訂單明細）
CALL generate_test_orders(50000);
```

### 3.2 JOIN 效能測試 - 無索引

```sql
-- 步驟 3: 測試 JOIN 查詢（無索引）

EXPLAIN 
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    COUNT(o.order_id) AS order_count,
    COALESCE(SUM(o.total_amount), 0) AS total_spent
FROM test_customers c
LEFT JOIN test_orders o ON c.customer_id = o.customer_id
WHERE c.country = 'USA'
GROUP BY c.customer_id, c.first_name, c.last_name
ORDER BY total_spent DESC
LIMIT 20;

-- 🔍 觀察 EXPLAIN 結果：
-- test_orders 的 type: ALL (全表掃描！)

SET profiling = 1;

SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    COUNT(o.order_id) AS order_count,
    COALESCE(SUM(o.total_amount), 0) AS total_spent
FROM test_customers c
LEFT JOIN test_orders o ON c.customer_id = o.customer_id
WHERE c.country = 'USA'
GROUP BY c.customer_id, c.first_name, c.last_name
ORDER BY total_spent DESC
LIMIT 20;

SHOW PROFILES;

-- 📝 記錄時間：_______ seconds
```

### 3.3 優化 JOIN - 建立適當索引

```sql
-- 步驟 4: 建立 JOIN 所需的索引

CREATE INDEX idx_customer_id ON test_orders(customer_id);

SHOW INDEX FROM test_orders;
```

```sql
-- 步驟 5: 重新測試 JOIN 查詢（有索引）

EXPLAIN 
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    COUNT(o.order_id) AS order_count,
    COALESCE(SUM(o.total_amount), 0) AS total_spent
FROM test_customers c
LEFT JOIN test_orders o ON c.customer_id = o.customer_id
WHERE c.country = 'USA'
GROUP BY c.customer_id, c.first_name, c.last_name
ORDER BY total_spent DESC
LIMIT 20;

-- 🔍 觀察改善：type: ref

SET profiling = 1;

SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    COUNT(o.order_id) AS order_count,
    COALESCE(SUM(o.total_amount), 0) AS total_spent
FROM test_customers c
LEFT JOIN test_orders o ON c.customer_id = o.customer_id
WHERE c.country = 'USA'
GROUP BY c.customer_id, c.first_name, c.last_name
ORDER BY total_spent DESC
LIMIT 20;

SHOW PROFILES;

-- 📝 記錄時間：_______ seconds (改善: ___%)
```

**💡 學習重點：**
1. ✅ JOIN 的欄位必須建立索引
2. ✅ MySQL 優化器會選擇最佳的 JOIN 順序
3. ✅ 小表驅動大表通常更快
4. ⚠️ 避免在 JOIN 條件中使用函數

---

## 4. 子查詢 vs JOIN 效能比較

### 4.1 場景：找出有訂單的客戶

```sql
-- 步驟 1: 使用 IN 子查詢

EXPLAIN
SELECT customer_id, first_name, last_name, email
FROM test_customers
WHERE customer_id IN (
    SELECT DISTINCT customer_id 
    FROM test_orders 
    WHERE order_date >= '2025-01-01'
);

SET profiling = 1;

SELECT customer_id, first_name, last_name, email
FROM test_customers
WHERE customer_id IN (
    SELECT DISTINCT customer_id 
    FROM test_orders 
    WHERE order_date >= '2025-01-01'
);

SHOW PROFILES;
-- 📝 子查詢方式時間：_______ seconds
```

```sql
-- 步驟 2: 使用 JOIN 改寫

EXPLAIN
SELECT DISTINCT c.customer_id, c.first_name, c.last_name, c.email
FROM test_customers c
INNER JOIN test_orders o ON c.customer_id = o.customer_id
WHERE o.order_date >= '2025-01-01';

SET profiling = 1;

SELECT DISTINCT c.customer_id, c.first_name, c.last_name, c.email
FROM test_customers c
INNER JOIN test_orders o ON c.customer_id = o.customer_id
WHERE o.order_date >= '2025-01-01';

SHOW PROFILES;
-- 📝 JOIN 方式時間：_______ seconds
```

```sql
-- 步驟 3: 使用 EXISTS（通常最快）

EXPLAIN
SELECT c.customer_id, c.first_name, c.last_name, c.email
FROM test_customers c
WHERE EXISTS (
    SELECT 1 
    FROM test_orders o
    WHERE o.customer_id = c.customer_id 
        AND o.order_date >= '2025-01-01'
);

SET profiling = 1;

SELECT c.customer_id, c.first_name, c.last_name, c.email
FROM test_customers c
WHERE EXISTS (
    SELECT 1 
    FROM test_orders o
    WHERE o.customer_id = c.customer_id 
        AND o.order_date >= '2025-01-01'
);

SHOW PROFILES;
-- 📝 EXISTS 方式時間：_______ seconds
```

**💡 學習重點：**
1. ✅ EXISTS 通常比 IN 子查詢快（特別是大表）
2. ✅ JOIN 的語意更清楚，功能更強大
3. ✅ 避免在 WHERE 子句中使用不相關的子查詢
4. ⚠️ IN 子查詢在小資料集時可接受

---

## 5. WHERE 條件順序與索引使用

### 5.1 建立複合條件測試

```sql
-- 步驟 1: 為測試表建立多個單一欄位索引

CREATE INDEX idx_city ON test_customers(city);
CREATE INDEX idx_registration_date ON test_customers(registration_date);
CREATE INDEX idx_total_spent ON test_customers(total_spent);

SHOW INDEX FROM test_customers;
```

### 5.2 測試條件順序

```sql
-- 步驟 2: 測試不同的 WHERE 條件順序

-- 🧪 測試 A
EXPLAIN
SELECT customer_id, first_name, last_name, total_spent
FROM test_customers
WHERE country = 'USA'
    AND city = 'New York'
    AND registration_date >= '2024-01-01'
    AND total_spent > 1000;

-- 🧪 測試 B (改變順序)
EXPLAIN
SELECT customer_id, first_name, last_name, total_spent
FROM test_customers
WHERE registration_date >= '2024-01-01'
    AND city = 'New York'
    AND country = 'USA'
    AND total_spent > 1000;
```

**🔍 觀察：** MySQL 優化器會自動選擇最佳的索引，WHERE 條件順序通常不影響效能

### 5.3 索引選擇性分析

```sql
-- 步驟 3: 分析各欄位的選擇性

SELECT 
    'country' AS column_name,
    COUNT(DISTINCT country) AS distinct_values,
    COUNT(*) AS total_rows,
    ROUND(COUNT(DISTINCT country) / COUNT(*) * 100, 2) AS selectivity_percent
FROM test_customers

UNION ALL

SELECT 
    'city',
    COUNT(DISTINCT city),
    COUNT(*),
    ROUND(COUNT(DISTINCT city) / COUNT(*) * 100, 2)
FROM test_customers

UNION ALL

SELECT 
    'registration_date',
    COUNT(DISTINCT registration_date),
    COUNT(*),
    ROUND(COUNT(DISTINCT registration_date) / COUNT(*) * 100, 2)
FROM test_customers;

-- 📊 選擇性越高（接近 100%）越適合建立索引
```

**💡 學習重點：**
1. ✅ 高選擇性欄位適合建立索引
2. ✅ MySQL 優化器會自動選擇最佳索引
3. ✅ WHERE 條件順序不影響效能

---

## 6. 複合索引的使用策略

### 6.1 理解複合索引的「最左前綴」原則

```sql
-- 步驟 1: 建立複合索引

DROP INDEX idx_country ON test_customers;
DROP INDEX idx_city ON test_customers;

CREATE INDEX idx_country_city_date 
ON test_customers(country, city, registration_date);

SHOW INDEX FROM test_customers;
```

### 6.2 測試最左前綴原則

```sql
-- 步驟 2: 測試哪些查詢能使用這個索引

-- ✅ 測試 1: 使用 country（最左欄位）
EXPLAIN
SELECT * FROM test_customers WHERE country = 'USA';
-- 結果: 使用索引 ✓

-- ✅ 測試 2: 使用 country + city
EXPLAIN
SELECT * FROM test_customers WHERE country = 'USA' AND city = 'New York';
-- 結果: 使用索引 ✓

-- ✅ 測試 3: 使用 country + city + date
EXPLAIN
SELECT * FROM test_customers 
WHERE country = 'USA' 
    AND city = 'New York' 
    AND registration_date >= '2024-01-01';
-- 結果: 使用索引 ✓

-- ❌ 測試 4: 只使用 city（跳過最左欄位）
EXPLAIN
SELECT * FROM test_customers WHERE city = 'New York';
-- 結果: 不使用索引或效果不佳 ✗

-- ⚠️ 測試 5: 使用 country + date（跳過中間欄位）
EXPLAIN
SELECT * FROM test_customers 
WHERE country = 'USA' 
    AND registration_date >= '2024-01-01';
-- 結果: 部分使用索引（只用到 country）△
```

**💡 學習重點：**
1. ✅ 複合索引遵循「最左前綴」原則
2. ✅ 索引欄位順序很重要：(A,B,C) ≠ (C,B,A)
3. ✅ 高選擇性欄位放在前面
4. ⚠️ 過多索引會降低寫入效能

---

## 7. 聚合查詢優化

### 7.1 GROUP BY 效能優化

```sql
-- 步驟 1: 測試沒有索引的聚合查詢

EXPLAIN
SELECT 
    order_id,
    COUNT(*) AS item_count,
    SUM(quantity * price) AS total
FROM test_order_items
GROUP BY order_id
ORDER BY total DESC
LIMIT 20;

-- 🔍 觀察: type = ALL, Extra = Using filesort

SET profiling = 1;

SELECT 
    order_id,
    COUNT(*) AS item_count,
    SUM(quantity * price) AS total
FROM test_order_items
GROUP BY order_id
ORDER BY total DESC
LIMIT 20;

SHOW PROFILES;
-- 📝 時間：_______ seconds
```

```sql
-- 步驟 2: 建立索引後重新測試

CREATE INDEX idx_order_id ON test_order_items(order_id);

EXPLAIN
SELECT 
    order_id,
    COUNT(*) AS item_count,
    SUM(quantity * price) AS total
FROM test_order_items
GROUP BY order_id
ORDER BY total DESC
LIMIT 20;

-- 🔍 觀察改善: type = index

SET profiling = 1;

SELECT 
    order_id,
    COUNT(*) AS item_count,
    SUM(quantity * price) AS total
FROM test_order_items
GROUP BY order_id
ORDER BY total DESC
LIMIT 20;

SHOW PROFILES;
-- 📝 時間：_______ seconds (改善: ___%)
```

**💡 學習重點：**
1. ✅ GROUP BY 的欄位應該建立索引
2. ✅ 避免在 GROUP BY 使用函數
3. ✅ 覆蓋索引可以避免回表

---

## 8. LIMIT 與分頁查詢優化

### 8.1 深度分頁問題

```sql
-- 步驟 1: 測試不同的分頁位置

SET profiling = 1;

-- 第一頁（快）
SELECT customer_id, first_name, last_name, email
FROM test_customers
ORDER BY customer_id
LIMIT 20 OFFSET 0;

-- 中間頁（變慢）
SELECT customer_id, first_name, last_name, email
FROM test_customers
ORDER BY customer_id
LIMIT 20 OFFSET 50000;

-- 最後頁（很慢！）
SELECT customer_id, first_name, last_name, email
FROM test_customers
ORDER BY customer_id
LIMIT 20 OFFSET 99980;

SHOW PROFILES;

-- 📝 記錄時間：
-- 第一頁: _______ seconds
-- 中間頁: _______ seconds
-- 最後頁: _______ seconds
```

### 8.2 使用鍵值分頁（Key-based Pagination）

```sql
-- 步驟 2: 優化深度分頁

-- ❌ 傳統分頁（慢）
SELECT customer_id, first_name, last_name, email
FROM test_customers
ORDER BY customer_id
LIMIT 20 OFFSET 50000;

-- ✅ 鍵值分頁（快！）
SELECT customer_id, first_name, last_name, email
FROM test_customers
WHERE customer_id > 50020  -- 使用上一頁的最後 ID
ORDER BY customer_id
LIMIT 20;

-- 測試效能差異
SET profiling = 1;

SELECT customer_id, first_name, last_name, email
FROM test_customers
ORDER BY customer_id
LIMIT 20 OFFSET 50000;

SELECT customer_id, first_name, last_name, email
FROM test_customers
WHERE customer_id > 50020
ORDER BY customer_id
LIMIT 20;

SHOW PROFILES;

-- 📝 效能比較：
-- 傳統 OFFSET: _______ seconds
-- 鍵值分頁: _______ seconds (快約 ___倍)
```

**💡 學習重點：**
1. ✅ 避免使用大的 OFFSET
2. ✅ 使用鍵值分頁（記住上一頁的最後 ID）
3. ⚠️ 鍵值分頁無法跳頁

---

## 9. 避免全表掃描的技巧

### 9.1 識別導致全表掃描的查詢

```sql
-- 步驟 1: 常見的全表掃描案例

-- ❌ 案例 1: 在索引欄位上使用函數
EXPLAIN
SELECT * FROM test_customers
WHERE YEAR(registration_date) = 2024;
-- 結果: type = ALL

-- ✅ 改善方式
EXPLAIN
SELECT * FROM test_customers
WHERE registration_date >= '2024-01-01' 
    AND registration_date < '2025-01-01';
-- 結果: type = range

-- ❌ 案例 2: 使用 NOT IN
EXPLAIN
SELECT * FROM test_customers
WHERE customer_id NOT IN (
    SELECT customer_id FROM test_orders WHERE status = 'completed'
);

-- ✅ 改善方式：使用 NOT EXISTS
EXPLAIN
SELECT c.*
FROM test_customers c
WHERE NOT EXISTS (
    SELECT 1 FROM test_orders o
    WHERE o.customer_id = c.customer_id 
        AND o.status = 'completed'
);
```

### 9.2 LIKE 查詢優化

```sql
-- 步驟 2: LIKE 查詢的索引使用

CREATE INDEX idx_email ON test_customers(email);

-- ✅ 可以使用索引：前綴匹配
EXPLAIN
SELECT * FROM test_customers
WHERE email LIKE 'john%';
-- 結果: type = range

-- ❌ 無法使用索引：後綴匹配
EXPLAIN
SELECT * FROM test_customers
WHERE email LIKE '%@example.com';
-- 結果: type = ALL
```

**💡 學習重點：**
1. ✅ 避免在索引欄位上使用函數
2. ✅ LIKE 'xxx%' 可以使用索引，'%xxx' 不行
3. ✅ NOT EXISTS 通常比 NOT IN 快

---

## 10. 實戰案例：優化慢查詢

### 10.1 案例背景

原始慢查詢：
```sql
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    COUNT(DISTINCT o.order_id) AS total_orders,
    COALESCE(SUM(oi.quantity * oi.price), 0) AS total_spent,
    MAX(o.order_date) AS last_order_date
FROM test_customers c
LEFT JOIN test_orders o ON c.customer_id = o.customer_id
LEFT JOIN test_order_items oi ON o.order_id = oi.order_id
WHERE c.registration_date >= '2024-01-01'
    AND (o.status = 'completed' OR o.status IS NULL)
GROUP BY c.customer_id, c.first_name, c.last_name, c.email
HAVING total_spent > 0
ORDER BY total_spent DESC
LIMIT 100;
```

### 10.2 步驟 1: 分析問題

```sql
EXPLAIN
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    COUNT(DISTINCT o.order_id) AS total_orders,
    COALESCE(SUM(oi.quantity * oi.price), 0) AS total_spent,
    MAX(o.order_date) AS last_order_date
FROM test_customers c
LEFT JOIN test_orders o ON c.customer_id = o.customer_id
LEFT JOIN test_order_items oi ON o.order_id = oi.order_id
WHERE c.registration_date >= '2024-01-01'
    AND (o.status = 'completed' OR o.status IS NULL)
GROUP BY c.customer_id, c.first_name, c.last_name, c.email
HAVING total_spent > 0
ORDER BY total_spent DESC
LIMIT 100;

SET profiling = 1;
-- [執行上面的查詢]
SHOW PROFILES;

-- 📝 原始執行時間：_______ seconds
```

### 10.3 步驟 2: 建立必要的索引

```sql
-- 優化 1: 建立缺失的索引

CREATE INDEX idx_orders_customer_status 
ON test_orders(customer_id, status, order_date);

CREATE INDEX idx_customers_reg_date 
ON test_customers(registration_date);

-- 再次執行並測量
SET profiling = 1;
-- [執行原始查詢]
SHOW PROFILES;

-- 📝 優化後時間：_______ seconds (改善: ___%)
```

### 10.4 步驟 3: 重寫查詢

```sql
-- 優化 2: 使用子查詢減少 JOIN 的資料量

SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    COALESCE(order_stats.total_orders, 0) AS total_orders,
    COALESCE(order_stats.total_spent, 0) AS total_spent,
    order_stats.last_order_date
FROM test_customers c
INNER JOIN (
    SELECT 
        o.customer_id,
        COUNT(DISTINCT o.order_id) AS total_orders,
        SUM(oi.quantity * oi.price) AS total_spent,
        MAX(o.order_date) AS last_order_date
    FROM test_orders o
    INNER JOIN test_order_items oi ON o.order_id = oi.order_id
    WHERE o.status = 'completed'
    GROUP BY o.customer_id
    HAVING total_spent > 0
) AS order_stats ON c.customer_id = order_stats.customer_id
WHERE c.registration_date >= '2024-01-01'
ORDER BY order_stats.total_spent DESC
LIMIT 100;

SET profiling = 1;
-- [執行優化後的查詢]
SHOW PROFILES;

-- 📝 重寫後時間：_______ seconds (改善: ___%)
```

### 10.5 效能優化總結

| 優化階段 | 執行時間 | 改善幅度 | 關鍵技術 |
|---------|---------|---------|---------|
| 原始查詢 | ___s | - | - |
| 建立索引 | ___s | ___% | 適當索引 |
| 重寫查詢 | ___s | ___% | 子查詢優化 |

---

## 11. 清理測試資料

```sql
-- 清理測試表和資料
DROP TABLE IF EXISTS test_order_items;
DROP TABLE IF EXISTS test_orders;
DROP TABLE IF EXISTS test_customers;

-- 清理儲存過程
DROP PROCEDURE IF EXISTS generate_test_customers;
DROP PROCEDURE IF EXISTS generate_test_orders;

-- 確認清理完成
SHOW TABLES LIKE 'test_%';

SELECT '✅ 清理完成！' AS Status;
```

---

## 12. 學習檢查清單 ✅

完成本教學後，你應該能夠：

- [ ] 理解並使用 EXPLAIN 分析查詢計畫
- [ ] 識別全表掃描並知道如何優化
- [ ] 理解索引的原理和使用時機
- [ ] 掌握複合索引的最左前綴原則
- [ ] 知道何時使用 JOIN、子查詢或 EXISTS
- [ ] 優化 GROUP BY 和聚合查詢
- [ ] 解決深度分頁效能問題
- [ ] 重寫慢查詢並測量改善效果

---

## 13. 常見問題 FAQ

**Q1: 是不是索引越多越好？**
❌ 不是。每個索引都需要額外的儲存空間和維護開銷。

**Q2: 什麼時候不應該使用索引？**
- 表很小（< 1000 行）
- 欄位選擇性很低（如性別）
- 頻繁更新的欄位

**Q3: 索引失效的常見原因？**
1. 在索引欄位上使用函數
2. 使用 != 或 NOT IN
3. LIKE '%xxx' (後綴匹配)
4. 隱式類型轉換

---

**🎉 恭喜完成 SQL 查詢效能優化實戰教學！**

*最後更新：2026-03-15*
