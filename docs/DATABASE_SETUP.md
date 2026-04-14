# 北風資料庫 MySQL 練習環境

完整的 SQL 學習環境，基於經典的 Northwind Database，包含 100,000+ 筆訂單資料用於效能測試和練習。

## 📋 目錄

- [快速開始](#快速開始)
- [環境配置](#環境配置)
- [資料庫結構](#資料庫結構)
- [連接方式](#連接方式)
- [練習指南](#練習指南)
- [效能測試](#效能測試)
- [常見問題](#常見問題)
- [重置環境](#重置環境)

---

## 🚀 快速開始

### 1. 啟動資料庫

```bash
# 首次啟動（會自動初始化資料庫，需要 2-5 分鐘）
docker-compose up -d

# 查看初始化進度
docker-compose logs -f db
```

**注意事項：**
- 首次啟動會執行所有初始化腳本，包括生成 100,000+ 筆測試資料
- 請耐心等待直到看到 "ready for connections" 訊息
- 初始化完成後，資料會持久化保存

### 2. 驗證安裝

```bash
# 從容器內連接
docker-compose exec db mysql -u student -pstudent123 northwind

# 或從宿主機連接（需安裝 MySQL 客戶端）
mysql -h 127.0.0.1 -P 3306 -u student -pstudent123 northwind
```

### 3. 檢查資料

連接後執行：

```sql
-- 顯示所有表
SHOW TABLES;

-- 檢查資料量
SELECT 'Categories' AS TableName, COUNT(*) AS RecordCount FROM Categories
UNION ALL SELECT 'Suppliers', COUNT(*) FROM Suppliers
UNION ALL SELECT 'Products', COUNT(*) FROM Products
UNION ALL SELECT 'Customers', COUNT(*) FROM Customers
UNION ALL SELECT 'Employees', COUNT(*) FROM Employees
UNION ALL SELECT 'Orders', COUNT(*) FROM Orders
UNION ALL SELECT 'OrderDetails', COUNT(*) FROM OrderDetails
UNION ALL SELECT 'PerformanceTest', COUNT(*) FROM PerformanceTest;
```

**預期結果：**
- Categories: 8 筆
- Suppliers: 29 筆
- Products: 77 筆
- Customers: 91 筆
- Employees: 9 筆
- **Orders: 100,000+ 筆**
- **OrderDetails: 400,000+ 筆**
- **PerformanceTest: 1,000,000 筆**

---

## ⚙️ 環境配置

### 連接資訊

| 項目 | 值 |
|------|-----|
| **主機** | `localhost` 或 `127.0.0.1` |
| **端口** | `3306` |
| **資料庫** | `northwind` |
| **使用者名稱** | `student` |
| **密碼** | `student123` |
| **Root 密碼** | `123456` |

### 環境變數

可以複製 `.env.example` 為 `.env` 並自定義：

```bash
cp .env.example .env
```

編輯 `.env`：
```env
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_USER=your_username
MYSQL_PASSWORD=your_password
MYSQL_DATABASE=northwind
```

### 配置檔案位置

- **Docker Compose**: `docker-compose.yml`
- **MySQL 配置**: `mysql-conf/my.cnf`
- **初始化腳本**: `mysql-init/` 目錄
  - `02-northwind-schema.sql` - 資料庫結構
  - `03-northwind-data.sql` - 基礎資料
  - `04-generate-performance-data.sql` - 大量測試資料
  - `05-index-examples.sql` - 索引範例（可選執行）
  - `06-practice-queries.sql` - 練習查詢（可選執行）

---

## 📊 資料庫結構

### 核心表格

北風資料庫模擬一個食品進出口公司的業務系統：

#### 1. **Categories** (產品類別)
- 8 個類別：飲料、調味品、糖果點心、乳製品等
- 關係：一對多 → Products

#### 2. **Suppliers** (供應商)
- 29 家供應商，分佈於全球各地
- 包含聯絡資訊、地址等
- 關係：一對多 → Products

#### 3. **Products** (產品)
- 77 種產品
- 包含價格、庫存、供應商、類別等資訊
- 關係：
  - 多對一 → Categories
  - 多對一 → Suppliers
  - 多對多 ↔ Orders (透過 OrderDetails)

#### 4. **Customers** (客戶)
- 91 個客戶
- CustomerID 為 5 位字母代碼（例如：'ALFKI'）
- 關係：一對多 → Orders

#### 5. **Employees** (員工)
- 9 位員工
- **包含層級關係** (ReportsTo)：可練習自聯結
- 關係：
  - 自聯結 → Employees (主管關係)
  - 一對多 → Orders

#### 6. **Shippers** (物流公司)
- 3 家運輸公司
- 關係：一對多 → Orders

#### 7. **Orders** (訂單)
- **100,000+ 筆訂單**（已生成大量測試資料）
- 訂單日期範圍：2022-2026
- 關係：
  - 多對一 → Customers
  - 多對一 → Employees
  - 多對一 → Shippers
  - 一對多 → OrderDetails

#### 8. **OrderDetails** (訂單明細)
- **400,000+ 筆明細**
- 訂單與產品的多對多關聯表
- 包含數量、單價、折扣
- 關係：
  - 多對一 → Orders
  - 多對一 → Products

#### 9. **PerformanceTest** (效能測試表)
- **1,000,000 筆記錄**
- 專門用於索引效能比較
- 包含有索引和無索引的欄位

#### 其他輔助表
- **Region**, **Territories**, **EmployeeTerritories** - 銷售區域管理
- **CustomerDemographics**, **CustomerCustomerDemo** - 客戶分類

### 表關係圖（簡化）

```
Categories ──┐
             ├──> Products ──┐
Suppliers ───┘               │
                             ├──> OrderDetails ──> Orders ──┐
                             │                               │
Customers ───────────────────┼───────────────────────────────┤
Employees ───────────────────┼───────────────────────────────┤
Shippers ────────────────────┘                               ┘
```

### 視圖 (Views)

系統已創建以下視圖方便查詢：

```sql
-- 1. 產品詳細資訊（含類別和供應商）
SELECT * FROM ProductDetails LIMIT 10;

-- 2. 訂單摘要（含客戶和員工）
SELECT * FROM OrderSummary LIMIT 10;

-- 3. 銷售額統計
SELECT * FROM SalesStatistics LIMIT 10;
```

### 儲存過程 (Stored Procedures)

```sql
-- 查詢特定類別的產品
CALL GetProductsByCategory(1);

-- 查詢員工業績
CALL GetEmployeeSales(1, '2024-01-01', '2024-12-31');
```

---

## 🔌 連接方式

### 方法 1: MySQL 命令列客戶端

```bash
# 從宿主機連接
mysql -h 127.0.0.1 -P 3306 -u student -pstudent123 northwind

# 從容器內連接
docker-compose exec db mysql -u student -pstudent123 northwind
```

### 方法 2: MySQL Workbench

1. 開啟 MySQL Workbench
2. 新建連接：
   - Connection Name: `Northwind Docker`
   - Hostname: `127.0.0.1`
   - Port: `3306`
   - Username: `student`
   - Password: `student123`
   - Default Schema: `northwind`

### 方法 3: DBeaver

1. 新建連接 → MySQL
2. 填入連接資訊（同上）
3. 測試連接 → 確認

### 方法 4: VS Code (SQLTools 擴展)

1. 安裝 SQLTools 和 MySQL/MariaDB 驅動
2. 新建連接：
   ```json
   {
     "name": "Northwind",
     "server": "localhost",
     "port": 3306,
     "database": "northwind",
     "username": "student",
     "password": "student123"
   }
   ```

### 方法 5: 程式語言連接

#### Python (pymysql)
```python
import pymysql

connection = pymysql.connect(
    host='127.0.0.1',
    port=3306,
    user='student',
    password='student123',
    database='northwind',
    charset='utf8mb4'
)

cursor = connection.cursor()
cursor.execute("SELECT * FROM Products LIMIT 5")
results = cursor.fetchall()
print(results)
```

#### Java (JDBC)
```java
String url = "jdbc:mysql://localhost:3306/northwind";
String user = "student";
String password = "student123";

Connection conn = DriverManager.getConnection(url, user, password);
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT * FROM Products LIMIT 5");
```

#### Node.js (mysql2)
```javascript
const mysql = require('mysql2');

const connection = mysql.createConnection({
  host: 'localhost',
  port: 3306,
  user: 'student',
  password: 'student123',
  database: 'northwind'
});

connection.query('SELECT * FROM Products LIMIT 5', (err, results) => {
  console.log(results);
});
```

---

## 📚 練習指南

### 基礎練習（初學者）

#### 1. CRUD 操作

```sql
-- CREATE: 新增測試類別
INSERT INTO Categories (CategoryName, Description)
VALUES ('Test Category', 'This is for practice');

-- READ: 查詢所有飲料
SELECT * FROM Products WHERE CategoryID = 1;

-- UPDATE: 更新產品價格
UPDATE Products SET UnitPrice = 20.00 WHERE ProductID = 1;

-- DELETE: 刪除測試類別
DELETE FROM Categories WHERE CategoryName = 'Test Category';
```

#### 2. WHERE 條件查詢

```sql
-- 價格範圍
SELECT ProductName, UnitPrice FROM Products 
WHERE UnitPrice BETWEEN 10 AND 30;

-- 多個條件
SELECT * FROM Orders 
WHERE OrderDate >= '2024-01-01' 
  AND OrderDate < '2025-01-01' 
  AND CustomerID = 'ALFKI';

-- 模糊搜尋
SELECT ProductName FROM Products 
WHERE ProductName LIKE '%Chocolate%';
```

#### 3. 排序與限制

```sql
-- 價格由高到低
SELECT ProductName, UnitPrice FROM Products 
ORDER BY UnitPrice DESC 
LIMIT 10;

-- 多欄位排序
SELECT * FROM Products 
ORDER BY CategoryID ASC, UnitPrice DESC;
```

### 中級練習

#### 4. 聚合函數與分組

```sql
-- 每個類別的產品數量和平均價格
SELECT 
    CategoryID,
    COUNT(*) AS ProductCount,
    AVG(UnitPrice) AS AvgPrice,
    MIN(UnitPrice) AS MinPrice,
    MAX(UnitPrice) AS MaxPrice
FROM Products
GROUP BY CategoryID;

-- HAVING 過濾
SELECT CategoryID, AVG(UnitPrice) AS AvgPrice
FROM Products
GROUP BY CategoryID
HAVING AVG(UnitPrice) > 20;
```

#### 5. JOIN 聯結查詢

```sql
-- INNER JOIN: 產品與類別
SELECT p.ProductName, c.CategoryName, p.UnitPrice
FROM Products p
INNER JOIN Categories c ON p.CategoryID = c.CategoryID
WHERE p.UnitPrice > 30;

-- LEFT JOIN: 找出沒有訂單的客戶
SELECT c.CompanyName, COUNT(o.OrderID) AS OrderCount
FROM Customers c
LEFT JOIN Orders o ON c.CustomerID = o.CustomerID
GROUP BY c.CustomerID, c.CompanyName
HAVING OrderCount = 0;

-- 自聯結: 員工及其主管
SELECT 
    e.FirstName AS Employee,
    m.FirstName AS Manager
FROM Employees e
LEFT JOIN Employees m ON e.ReportsTo = m.EmployeeID;
```

#### 6. 子查詢

```sql
-- 價格高於平均的產品
SELECT ProductName, UnitPrice
FROM Products
WHERE UnitPrice > (SELECT AVG(UnitPrice) FROM Products);

-- IN 子查詢
SELECT CompanyName FROM Customers
WHERE CustomerID IN (
    SELECT DISTINCT CustomerID FROM Orders
    WHERE OrderDate >= '2024-01-01'
);
```

### 進階練習

#### 7. 複雜查詢

```sql
-- 銷售排行榜
SELECT 
    p.ProductName,
    SUM(od.Quantity) AS TotalQuantity,
    SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS TotalRevenue
FROM Products p
INNER JOIN OrderDetails od ON p.ProductID = od.ProductID
GROUP BY p.ProductID, p.ProductName
ORDER BY TotalRevenue DESC
LIMIT 10;

-- 員工業績統計
SELECT 
    CONCAT(e.FirstName, ' ', e.LastName) AS EmployeeName,
    COUNT(DISTINCT o.OrderID) AS OrderCount,
    SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS TotalSales
FROM Employees e
LEFT JOIN Orders o ON e.EmployeeID = o.EmployeeID
LEFT JOIN OrderDetails od ON o.OrderID = od.OrderID
GROUP BY e.EmployeeID
ORDER BY TotalSales DESC;

-- 月度銷售趨勢
SELECT 
    YEAR(o.OrderDate) AS Year,
    MONTH(o.OrderDate) AS Month,
    COUNT(DISTINCT o.OrderID) AS OrderCount,
    SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS Revenue
FROM Orders o
INNER JOIN OrderDetails od ON o.OrderID = od.OrderID
GROUP BY YEAR(o.OrderDate), MONTH(o.OrderDate)
ORDER BY Year, Month;
```

#### 8. 窗口函數 (MySQL 8.0+)

```sql
-- 產品價格排名
SELECT 
    ProductName,
    CategoryID,
    UnitPrice,
    RANK() OVER (PARTITION BY CategoryID ORDER BY UnitPrice DESC) AS PriceRank
FROM Products;

-- 累計銷售額
SELECT 
    OrderDate,
    OrderID,
    Freight,
    SUM(Freight) OVER (ORDER BY OrderDate) AS CumulativeFreight
FROM Orders
ORDER BY OrderDate
LIMIT 100;
```

### 練習資源

#### 📋 練習題目（建議從這裡開始）
**[SQL_PRACTICE_QUESTIONS.md](SQL_PRACTICE_QUESTIONS.md)** - 中文練習題目集
- 100+ 道練習題，從基礎到挑戰
- 只有題目描述，讓你先自己思考
- 包含提示但不直接給答案
- 按難度分級：基礎 → 中級 → 進階 → 挑戰

**使用方式：**
1. 打開 [SQL_PRACTICE_QUESTIONS.md](SQL_PRACTICE_QUESTIONS.md)
2. 選擇適合你程度的題目
3. 嘗試自己寫出 SQL 查詢
4. 完成後對照下方的參考答案

#### 📘 參考答案
**mysql-init/06-practice-queries.sql** - 完整的 SQL 查詢範例

可以在 MySQL 客戶端中執行：
```sql
SOURCE /docker-entrypoint-initdb.d/06-practice-queries.sql;
```

---

## ⚡ 效能測試

### INDEX 效能比較指南

系統已準備專門的效能測試環境和腳本：

#### 執行索引測試

```bash
# 連接資料庫後執行
docker-compose exec db mysql -u student -pstudent123 northwind

# 在 MySQL 中執行測試腳本
SOURCE /docker-entrypoint-initdb.d/05-index-examples.sql;
```

#### 測試場景

**測試 1: 全表掃描 vs 索引查詢**

```sql
-- 1. 無索引查詢（全表掃描）
EXPLAIN SELECT * FROM PerformanceTest WHERE non_indexed_col = 123456;
-- 結果：type=ALL, rows≈1,000,000

-- 2. 有索引查詢
EXPLAIN SELECT * FROM PerformanceTest WHERE indexed_col = 123456;
-- 結果：type=ref, rows≈1
```

**測試 2: 實際執行時間比較**

```sql
SET profiling = 1;

-- 無索引查詢
SELECT COUNT(*) FROM PerformanceTest WHERE non_indexed_col = 123456;

-- 有索引查詢  
SELECT COUNT(*) FROM PerformanceTest WHERE indexed_col = 123456;

-- 查看執行時間
SHOW PROFILES;
-- 無索引通常需要 0.5-2 秒，有索引只需 0.001-0.01 秒！
```

**測試 3: 動態添加索引**

```sql
-- 添加索引前
EXPLAIN SELECT * FROM PerformanceTest 
WHERE non_indexed_col BETWEEN 100000 AND 100100;

-- 創建索引
CREATE INDEX idx_test ON PerformanceTest(non_indexed_col);

-- 添加索引後
EXPLAIN SELECT * FROM PerformanceTest 
WHERE non_indexed_col BETWEEN 100000 AND 100100;

-- 觀察 type 和 rows 的變化！

-- 刪除測試索引
DROP INDEX idx_test ON PerformanceTest;
```

**測試 4: JOIN 效能**

```sql
-- 100,000+ 訂單與客戶聯結
EXPLAIN SELECT o.OrderID, c.CompanyName
FROM Orders o
INNER JOIN Customers c ON o.CustomerID = c.CustomerID
WHERE o.OrderDate >= '2024-01-01'
LIMIT 1000;

-- 觀察：外鍵索引對 JOIN 效能的影響
```

**測試 5: 複合索引**

```sql
-- 單欄位查詢
EXPLAIN SELECT * FROM Orders 
WHERE OrderDate >= '2024-01-01';

-- 創建複合索引
CREATE INDEX idx_date_customer ON Orders(OrderDate, CustomerID);

-- 多欄位查詢（利用複合索引）
EXPLAIN SELECT * FROM Orders 
WHERE OrderDate >= '2024-01-01' AND CustomerID = 'ALFKI';

-- 清理
DROP INDEX idx_date_customer ON Orders;
```

#### 效能分析工具

**1. EXPLAIN - 查詢執行計劃**

```sql
EXPLAIN SELECT * FROM Orders WHERE OrderDate >= '2024-01-01';
```

重要欄位：
- **type**: 訪問類型
  - `ALL`: 全表掃描（最慢）
  - `index`: 索引掃描
  - `range`: 索引範圍掃描
  - `ref`: 索引查找
  - `const`: 常數查找（最快）
- **rows**: 預計掃描行數
- **key**: 使用的索引
- **Extra**: 額外資訊

**2. EXPLAIN ANALYZE - 實際執行分析**

```sql
EXPLAIN ANALYZE 
SELECT * FROM Orders WHERE OrderDate >= '2024-01-01' LIMIT 100;
```

顯示實際執行時間和行數。

**3. SHOW PROFILE - 效能剖析**

```sql
SET profiling = 1;
SELECT COUNT(*) FROM Orders WHERE EmployeeID = 5;
SHOW PROFILES;
SHOW PROFILE FOR QUERY 1;
```

**4. Performance Schema**

```sql
-- 查看慢查詢
SELECT * FROM performance_schema.events_statements_history_long
WHERE SQL_TEXT LIKE '%Orders%'
ORDER BY TIMER_WAIT DESC
LIMIT 10;
```

#### 查詢日誌

系統已啟用查詢日誌，可以查看所有執行的 SQL：

```bash
# 即時查看一般查詢日誌
docker-compose exec db tail -f /var/lib/mysql/general.log

# 查看慢查詢日誌
docker-compose exec db tail -f /var/lib/mysql/slow.log
```

#### 索引最佳實踐

✅ **適合建立索引的情況：**
- 經常用於 WHERE 條件的欄位
- JOIN 的關聯欄位（外鍵）
- ORDER BY 和 GROUP BY 的欄位
- 高選擇性欄位（值分散）

❌ **不適合建立索引：**
- 很少查詢的欄位
- 低選擇性欄位（如性別：只有兩個值）
- 資料量小的表（< 1000 筆）
- 頻繁更新的欄位

#### 效能測試練習題

1. 比較 CHAR/VARCHAR vs INT 索引效能
2. 測試複合索引的最左前綴原則
3. 分析覆蓋索引（Covering Index）的優勢
4. 對比 LIKE 查詢在有無索引時的差異
5. 測試大批量 INSERT 時索引的維護成本

---

## ❓ 常見問題

### Q1: 初始化需要多久？
**A:** 首次啟動需要 2-5 分鐘，具體取決於硬體效能。主要時間花在生成 100 萬筆測試資料。

### Q2: 如何確認初始化完成？
**A:** 查看日誌：
```bash
docker-compose logs db | grep "ready for connections"
```
看到此訊息表示初始化完成。

### Q3: 連接被拒絕 (Connection refused)
**A:** 可能原因：
1. 容器還未完全啟動 → 等待 30 秒後重試
2. 端口 3306 被佔用 → 檢查其他 MySQL 服務
3. 防火牆阻擋 → 檢查防火牆設定

```bash
# 檢查容器狀態
docker-compose ps

# 檢查端口佔用
lsof -i :3306  # macOS/Linux
netstat -ano | findstr :3306  # Windows
```

### Q4: 密碼錯誤
**A:** 確認使用正確的使用者名稱和密碼：
- 使用者：`student`
- 密碼：`student123`
- Root 密碼：`123456`

### Q5: 資料庫太大，如何縮小？
**A:** 可以刪除部分測試資料：
```sql
-- 刪除 PerformanceTest 表的資料
TRUNCATE TABLE PerformanceTest;

-- 或只保留部分 Orders
DELETE FROM Orders WHERE OrderID > 20000;
```

### Q6: 如何查看資料庫大小？
```sql
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'northwind'
GROUP BY table_schema;
```

### Q7: 如何備份資料庫？
```bash
# 備份整個資料庫
docker-compose exec db mysqldump -u student -pstudent123 northwind > backup.sql

# 只備份結構（不含資料）
docker-compose exec db mysqldump -u student -pstudent123 --no-data northwind > schema.sql

# 還原備份
docker-compose exec -T db mysql -u student -pstudent123 northwind < backup.sql
```

### Q8: 能否使用 root 使用者？
**A:** 可以，但不建議在日常練習中使用：
```bash
mysql -h 127.0.0.1 -P 3306 -u root -p123456 northwind
```

### Q9: 如何修改 MySQL 配置？
**A:** 編輯 `mysql-conf/my.cnf` 然後重啟：
```bash
# 修改配置後
docker-compose restart db
```

### Q10: 容器日誌太多怎麼辦？
**A:** 可以關閉一般查詢日誌（保留慢查詢日誌）：
```sql
SET GLOBAL general_log = 0;
```
或修改 `mysql-conf/my.cnf` 並重啟。

---

## 🔄 重置環境

### 完全重置（刪除所有資料）

```bash
# 停止並刪除容器和資料卷
docker-compose down -v

# 重新啟動（會重新初始化）
docker-compose up -d

# 等待初始化完成
docker-compose logs -f db
```

⚠️ **警告：這會刪除所有資料，包括你的練習修改！**

### 部分重置

#### 只重置測試資料

```sql
-- 刪除測試資料
TRUNCATE TABLE PerformanceTest;

-- 重新執行生成腳本
SOURCE /docker-entrypoint-initdb.d/04-generate-performance-data.sql;
```

#### 重置訂單資料

```sql
-- 刪除訂單（會級聯刪除 OrderDetails）
DELETE FROM Orders WHERE OrderID > 10252;

-- 重新生成
SOURCE /docker-entrypoint-initdb.d/04-generate-performance-data.sql;
```

#### 只重啟容器（保留資料）

```bash
docker-compose restart db
```

---

## 📁 專案結構

```
.
├── docker-compose.yml           # Docker Compose 配置
├── Dockerfile                   # Docker 映像定義（未使用）
├── .env.example                 # 環境變數範本
├── DATABASE_SETUP.md            # 完整使用手冊
├── SQL_PRACTICE_QUESTIONS.md    # 練習題目集 ⭐ 推薦從這裡開始！
├── mysql-conf/                  # MySQL 配置目錄
│   └── my.cnf                  # 自定義 MySQL 配置
└── mysql-init/                  # 初始化腳本目錄（按順序執行）
    ├── 02-northwind-schema.sql             # 資料庫結構
    ├── 03-northwind-data.sql               # 基礎資料
    ├── 04-generate-performance-data.sql    # 大量測試資料
    ├── 05-index-examples.sql               # 索引測試範例
    └── 06-practice-queries.sql             # 練習查詢參考答案
```

---

## 🎯 學習路徑建議

### 第 1 週：SQL 基礎
1. 基本 SELECT 查詢
2. WHERE 條件過濾
3. ORDER BY 排序
4. LIMIT 限制結果
5. 基本 CRUD 操作

**練習檔案：** `06-practice-queries.sql` Part 1-2

### 第 2 週：聚合與分組
1. COUNT, SUM, AVG, MAX, MIN
2. GROUP BY 分組
3. HAVING 過濾分組
4. 聚合函數組合使用

**練習檔案：** `06-practice-queries.sql` Part 4-5

### 第 3 週：表聯結
1. INNER JOIN
2. LEFT JOIN / RIGHT JOIN
3. 多表聯結（3+ 表）
4. 自聯結 (Self JOIN)
5. 子查詢

**練習檔案：** `06-practice-queries.sql` Part 6-7

### 第 4 週：效能優化
1. EXPLAIN 分析查詢計劃
2. 索引的使用和影響
3. 複合索引
4. 覆蓋索引
5. 查詢效能調優

**練習檔案：** `05-index-examples.sql`

### 第 5 週：進階主題
1. 視圖 (Views)
2. 儲存過程 (Stored Procedures)
3. 窗口函數 (Window Functions)
4. CTE (Common Table Expressions)
5. 交易處理

**練習檔案：** `06-practice-queries.sql` Part 8-10

---

## 📖 參考資源

### 官方文件
- [MySQL 8.0 Reference Manual](https://dev.mysql.com/doc/refman/8.0/en/)
- [MySQL Performance Schema](https://dev.mysql.com/doc/refman/8.0/en/performance-schema.html)

### 學習資源
- [SQLBolt](https://sqlbolt.com/) - 互動式 SQL 教學
- [Mode SQL Tutorial](https://mode.com/sql-tutorial/) - 完整的 SQL 課程
- [LeetCode Database](https://leetcode.com/problemset/database/) - SQL 練習題

### 效能優化
- [USE THE INDEX, LUKE](https://use-the-index-luke.com/) - 索引優化指南
- [MySQL Indexing Best Practices](https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html)

---

## 🤝 貢獻與回饋

如有問題或建議，歡迎提出 Issue 或 Pull Request。

---

## 📝 授權

本專案基於經典的 Northwind Database，僅供學習使用。

---

**祝學習愉快！Happy SQL Learning! 🎉**
