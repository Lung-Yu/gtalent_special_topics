-- ========================================
-- SQL Practice Queries Collection
-- SQL 練習查詢範例集合
-- 
-- 此腳本包含從基礎到進階的 SQL 查詢範例
-- 涵蓋 CRUD、JOIN、WHERE、GROUP BY、子查詢等所有重要概念
-- ========================================

USE northwind;

SELECT '========================================' AS '';
SELECT 'SQL Practice Queries Collection' AS '';
SELECT '========================================' AS '';
SELECT 'Copy and run these queries to practice!' AS '';
SELECT '從簡單到複雜，循序漸進學習 SQL' AS '';
SELECT '========================================' AS '';

-- ========================================
-- 第一部分：基本 SELECT 查詢
-- ========================================

SELECT '========================================' AS '';
SELECT 'Part 1: Basic SELECT Queries' AS '';
SELECT '========================================' AS '';

-- Q1.1: 查詢所有產品
-- SELECT * FROM Products;

-- Q1.2: 查詢特定欄位
-- SELECT ProductName, UnitPrice, UnitsInStock FROM Products;

-- Q1.3: 使用別名（Alias）
-- SELECT ProductName AS 產品名稱, UnitPrice AS 單價, UnitsInStock AS 庫存 FROM Products;

-- Q1.4: 計算欄位
-- SELECT ProductName, UnitPrice, UnitsInStock, (UnitPrice * UnitsInStock) AS 總庫存價值 FROM Products;

-- Q1.5: DISTINCT 去除重複
-- SELECT DISTINCT Country FROM Customers;

-- Q1.6: LIMIT 限制結果數量
-- SELECT * FROM Products LIMIT 10;

-- ========================================
-- 第二部分：WHERE 條件過濾
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 2: WHERE Clause' AS '';
SELECT '========================================' AS '';

-- Q2.1: 等於條件
-- SELECT * FROM Products WHERE CategoryID = 1;

-- Q2.2: 比較運算符
-- SELECT ProductName, UnitPrice FROM Products WHERE UnitPrice > 50;
-- SELECT ProductName, UnitPrice FROM Products WHERE UnitPrice <= 20;

-- Q2.3: BETWEEN 範圍查詢
-- SELECT ProductName, UnitPrice FROM Products WHERE UnitPrice BETWEEN 10 AND 30;

-- Q2.4: IN 多個值
-- SELECT * FROM Customers WHERE Country IN ('Germany', 'France', 'UK');

-- Q2.5: LIKE 模糊查詢
-- SELECT ProductName FROM Products WHERE ProductName LIKE 'Ch%';  -- 開頭是 Ch
-- SELECT ProductName FROM Products WHERE ProductName LIKE '%on%';  -- 包含 on
-- SELECT ProductName FROM Products WHERE ProductName LIKE '_h%';  -- 第二個字母是 h

-- Q2.6: IS NULL / IS NOT NULL
-- SELECT * FROM Orders WHERE ShippedDate IS NULL;  -- 未出貨的訂單
-- SELECT * FROM Orders WHERE ShippedDate IS NOT NULL;  -- 已出貨的訂單

-- Q2.7: AND / OR 邏輯運算
-- SELECT * FROM Products WHERE CategoryID = 1 AND UnitPrice > 15;
-- SELECT * FROM Products WHERE CategoryID = 1 OR CategoryID = 2;

-- Q2.8: NOT 否定條件
-- SELECT * FROM Products WHERE NOT Discontinued = 1;  -- 未停產的產品

-- Q2.9: 日期查詢
-- SELECT * FROM Orders WHERE OrderDate >= '2024-01-01' AND OrderDate < '2025-01-01';

-- Q2.10: 複雜組合條件
-- SELECT * FROM Products 
-- WHERE (CategoryID = 1 OR CategoryID = 2) 
--   AND UnitPrice > 10 
--   AND Discontinued = 0;

-- ========================================
-- 第三部分：ORDER BY 排序
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 3: ORDER BY Sorting' AS '';
SELECT '========================================' AS '';

-- Q3.1: 升序排序（預設）
-- SELECT ProductName, UnitPrice FROM Products ORDER BY UnitPrice;

-- Q3.2: 降序排序
-- SELECT ProductName, UnitPrice FROM Products ORDER BY UnitPrice DESC;

-- Q3.3: 多欄位排序
-- SELECT ProductName, CategoryID, UnitPrice 
-- FROM Products 
-- ORDER BY CategoryID ASC, UnitPrice DESC;

-- Q3.4: 依計算欄位排序
-- SELECT ProductName, UnitPrice, UnitsInStock, (UnitPrice * UnitsInStock) AS TotalValue
-- FROM Products 
-- ORDER BY TotalValue DESC;

-- ========================================
-- 第四部分：聚合函數 (Aggregate Functions)
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 4: Aggregate Functions' AS '';
SELECT '========================================' AS '';

-- Q4.1: COUNT 計數
-- SELECT COUNT(*) AS 總產品數 FROM Products;
-- SELECT COUNT(DISTINCT Country) AS 國家數量 FROM Customers;

-- Q4.2: SUM 總和
-- SELECT SUM(UnitsInStock) AS 總庫存量 FROM Products;

-- Q4.3: AVG 平均值
-- SELECT AVG(UnitPrice) AS 平均價格 FROM Products;

-- Q4.4: MAX / MIN 最大最小值
-- SELECT MAX(UnitPrice) AS 最高價, MIN(UnitPrice) AS 最低價 FROM Products;

-- Q4.5: 多個聚合函數
-- SELECT 
--     COUNT(*) AS 產品數,
--     AVG(UnitPrice) AS 平均價格,
--     MIN(UnitPrice) AS 最低價,
--     MAX(UnitPrice) AS 最高價
-- FROM Products;

-- ========================================
-- 第五部分：GROUP BY 分組
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 5: GROUP BY' AS '';
SELECT '========================================' AS '';

-- Q5.1: 基本分組 - 每個類別的產品數量
-- SELECT CategoryID, COUNT(*) AS 產品數量
-- FROM Products
-- GROUP BY CategoryID;

-- Q5.2: 分組聚合 - 每個類別的平均價格
-- SELECT CategoryID, AVG(UnitPrice) AS 平均價格
-- FROM Products
-- GROUP BY CategoryID;

-- Q5.3: 多欄位分組
-- SELECT CategoryID, Discontinued, COUNT(*) AS 產品數量
-- FROM Products
-- GROUP BY CategoryID, Discontinued
-- ORDER BY CategoryID, Discontinued;

-- Q5.4: HAVING 過濾分組結果（類似 GROUP BY 的 WHERE）
-- SELECT CategoryID, COUNT(*) AS 產品數量
-- FROM Products
-- GROUP BY CategoryID
-- HAVING COUNT(*) > 10;

-- Q5.5: WHERE + GROUP BY + HAVING
-- SELECT CategoryID, AVG(UnitPrice) AS 平均價格
-- FROM Products
-- WHERE Discontinued = 0  -- 先過濾：只看未停產
-- GROUP BY CategoryID
-- HAVING AVG(UnitPrice) > 20  -- 再過濾分組結果：平均價格 > 20
-- ORDER BY 平均價格 DESC;

-- Q5.6: 每個國家的客戶數量
-- SELECT Country, COUNT(*) AS 客戶數量
-- FROM Customers
-- GROUP BY Country
-- ORDER BY 客戶數量 DESC;

-- Q5.7: 每個員工的訂單數量
-- SELECT EmployeeID, COUNT(*) AS 訂單數量
-- FROM Orders
-- GROUP BY EmployeeID
-- ORDER BY 訂單數量 DESC;

-- ========================================
-- 第六部分：JOIN 表聯結
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 6: JOIN Operations' AS '';
SELECT '========================================' AS '';

-- Q6.1: INNER JOIN - 兩表聯結
-- SELECT p.ProductName, c.CategoryName
-- FROM Products p
-- INNER JOIN Categories c ON p.CategoryID = c.CategoryID;

-- Q6.2: 三表聯結
-- SELECT p.ProductName, c.CategoryName, s.CompanyName AS SupplierName
-- FROM Products p
-- INNER JOIN Categories c ON p.CategoryID = c.CategoryID
-- INNER JOIN Suppliers s ON p.SupplierID = s.SupplierID;

-- Q6.3: JOIN 帶 WHERE 條件
-- SELECT p.ProductName, c.CategoryName, p.UnitPrice
-- FROM Products p
-- INNER JOIN Categories c ON p.CategoryID = c.CategoryID
-- WHERE p.UnitPrice > 30
-- ORDER BY p.UnitPrice DESC;

-- Q6.4: LEFT JOIN - 顯示所有產品，包括沒有供應商的
-- SELECT p.ProductName, s.CompanyName AS SupplierName
-- FROM Products p
-- LEFT JOIN Suppliers s ON p.SupplierID = s.SupplierID;

-- Q6.5: 查找沒有訂單的客戶
-- SELECT c.CustomerID, c.CompanyName
-- FROM Customers c
-- LEFT JOIN Orders o ON c.CustomerID = o.CustomerID
-- WHERE o.OrderID IS NULL;

-- Q6.6: 自聯結 (Self JOIN) - 員工及其主管
-- SELECT 
--     e.FirstName AS 員工名字,
--     e.LastName AS 員工姓氏,
--     m.FirstName AS 主管名字,
--     m.LastName AS 主管姓氏
-- FROM Employees e
-- LEFT JOIN Employees m ON e.ReportsTo = m.EmployeeID;

-- Q6.7: 複雜四表聯結 - 訂單詳情
-- SELECT 
--     o.OrderID,
--     c.CompanyName AS 客戶,
--     p.ProductName AS 產品,
--     od.Quantity AS 數量,
--     od.UnitPrice AS 單價
-- FROM Orders o
-- INNER JOIN Customers c ON o.CustomerID = c.CustomerID
-- INNER JOIN OrderDetails od ON o.OrderID = od.OrderID
-- INNER JOIN Products p ON od.ProductID = p.ProductID
-- LIMIT 20;

-- Q6.8: 六表聯結 - 完整訂單資訊
-- SELECT 
--     o.OrderID,
--     o.OrderDate,
--     c.CompanyName AS 客戶,
--     CONCAT(e.FirstName, ' ', e.LastName) AS 負責員工,
--     p.ProductName AS 產品,
--     cat.CategoryName AS 類別,
--     s.CompanyName AS 供應商,
--     od.Quantity,
--     od.UnitPrice
-- FROM Orders o
-- INNER JOIN Customers c ON o.CustomerID = c.CustomerID
-- INNER JOIN Employees e ON o.EmployeeID = e.EmployeeID
-- INNER JOIN OrderDetails od ON o.OrderID = od.OrderID
-- INNER JOIN Products p ON od.ProductID = p.ProductID
-- INNER JOIN Categories cat ON p.CategoryID = cat.CategoryID
-- INNER JOIN Suppliers s ON p.SupplierID = s.SupplierID
-- LIMIT 50;

-- ========================================
-- 第七部分：子查詢 (Subqueries)
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 7: Subqueries' AS '';
SELECT '========================================' AS '';

-- Q7.1: WHERE 子查詢 - 價格高於平均價的產品
-- SELECT ProductName, UnitPrice
-- FROM Products
-- WHERE UnitPrice > (SELECT AVG(UnitPrice) FROM Products);

-- Q7.2: IN 子查詢 - 有訂單的客戶
-- SELECT CompanyName
-- FROM Customers
-- WHERE CustomerID IN (SELECT DISTINCT CustomerID FROM Orders);

-- Q7.3: NOT IN 子查詢 - 沒有訂單的客戶
-- SELECT CompanyName
-- FROM Customers
-- WHERE CustomerID NOT IN (SELECT DISTINCT CustomerID FROM Orders WHERE CustomerID IS NOT NULL);

-- Q7.4: EXISTS 子查詢
-- SELECT c.CompanyName
-- FROM Customers c
-- WHERE EXISTS (
--     SELECT 1 FROM Orders o WHERE o.CustomerID = c.CustomerID
-- );

-- Q7.5: FROM 子查詢（派生表）
-- SELECT CategoryID, AVG(TotalValue) AS 平均總值
-- FROM (
--     SELECT CategoryID, (UnitPrice * UnitsInStock) AS TotalValue
--     FROM Products
-- ) AS ProductValues
-- GROUP BY CategoryID;

-- ========================================
-- 第八部分：進階查詢
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 8: Advanced Queries' AS '';
SELECT '========================================' AS '';

-- Q8.1: CASE 條件表達式
-- SELECT 
--     ProductName,
--     UnitPrice,
--     CASE 
--         WHEN UnitPrice < 10 THEN '便宜'
--         WHEN UnitPrice < 50 THEN '中等'
--         ELSE '昂貴'
--     END AS 價格等級
-- FROM Products;

-- Q8.2: 日期函數
-- SELECT 
--     OrderID,
--     OrderDate,
--     YEAR(OrderDate) AS 年份,
--     MONTH(OrderDate) AS 月份,
--     DAY(OrderDate) AS 日期,
--     DAYNAME(OrderDate) AS 星期幾
-- FROM Orders
-- LIMIT 10;

-- Q8.3: 字串函數
-- SELECT 
--     CONCAT(FirstName, ' ', LastName) AS 全名,
--     UPPER(FirstName) AS 大寫名字,
--     LOWER(LastName) AS 小寫姓氏,
--     LENGTH(FirstName) AS 名字長度
-- FROM Employees;

-- Q8.4: WITH (CTE - Common Table Expression) MySQL 8.0+
-- WITH ExpensiveProducts AS (
--     SELECT * FROM Products WHERE UnitPrice > 50
-- )
-- SELECT p.ProductName, c.CategoryName
-- FROM ExpensiveProducts p
-- INNER JOIN Categories c ON p.CategoryID = c.CategoryID;

-- Q8.5: UNION 合併結果
-- SELECT 'Customer' AS Type, CompanyName AS Name, Country FROM Customers WHERE Country = 'USA'
-- UNION
-- SELECT 'Supplier', CompanyName, Country FROM Suppliers WHERE Country = 'USA';

-- Q8.6: 窗口函數 (Window Functions) MySQL 8.0+
-- SELECT 
--     ProductName,
--     CategoryID,
--     UnitPrice,
--     ROW_NUMBER() OVER (PARTITION BY CategoryID ORDER BY UnitPrice DESC) AS PriceRank
-- FROM Products;

-- Q8.7: 排名函數
-- SELECT 
--     ProductName,
--     UnitPrice,
--     DENSE_RANK() OVER (ORDER BY UnitPrice DESC) AS 價格排名
-- FROM Products
-- LIMIT 20;

-- ========================================
-- 第九部分：CRUD 操作練習
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 9: CRUD Operations Practice' AS '';
SELECT '========================================' AS '';
SELECT '注意：以下操作會修改資料，請謹慎使用！' AS '';
SELECT '建議先在測試環境或創建備份後再執行' AS '';

-- Q9.1: INSERT 插入資料
-- INSERT INTO Categories (CategoryName, Description)
-- VALUES ('Test Category', 'This is a test category');

-- Q9.2: 插入多筆資料
-- INSERT INTO Categories (CategoryName, Description) VALUES
-- ('Category A', 'Description A'),
-- ('Category B', 'Description B'),
-- ('Category C', 'Description C');

-- Q9.3: UPDATE 更新資料
-- UPDATE Products
-- SET UnitPrice = UnitPrice * 1.1  -- 價格上漲 10%
-- WHERE CategoryID = 1;

-- Q9.4: 帶條件的更新
-- UPDATE Products
-- SET Discontinued = 1
-- WHERE UnitsInStock = 0 AND UnitsOnOrder = 0;

-- Q9.5: DELETE 刪除資料
-- DELETE FROM Categories
-- WHERE CategoryName = 'Test Category';

-- Q9.6: 安全刪除（先查詢確認）
-- SELECT * FROM Categories WHERE CategoryName LIKE 'Test%';  -- 先查看
-- -- DELETE FROM Categories WHERE CategoryName LIKE 'Test%';  -- 確認後刪除

-- ========================================
-- 第十部分：實戰查詢範例
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Part 10: Real-world Query Examples' AS '';
SELECT '========================================' AS '';

-- Q10.1: 銷售排行 - 最暢銷的產品
-- SELECT 
--     p.ProductName,
--     SUM(od.Quantity) AS 總銷售量,
--     SUM(od.Quantity * od.UnitPrice) AS 總銷售額
-- FROM OrderDetails od
-- INNER JOIN Products p ON od.ProductID = p.ProductID
-- GROUP BY p.ProductID, p.ProductName
-- ORDER BY 總銷售額 DESC
-- LIMIT 10;

-- Q10.2: 員工業績統計
-- SELECT 
--     CONCAT(e.FirstName, ' ', e.LastName) AS 員工姓名,
--     COUNT(DISTINCT o.OrderID) AS 訂單數量,
--     SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS 總銷售額
-- FROM Employees e
-- LEFT JOIN Orders o ON e.EmployeeID = o.EmployeeID
-- LEFT JOIN OrderDetails od ON o.OrderID = od.OrderID
-- GROUP BY e.EmployeeID, e.FirstName, e.LastName
-- ORDER BY 總銷售額 DESC;

-- Q10.3: 月度銷售趨勢
-- SELECT 
--     YEAR(o.OrderDate) AS 年份,
--     MONTH(o.OrderDate) AS 月份,
--     COUNT(DISTINCT o.OrderID) AS 訂單數,
--     SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS 銷售額
-- FROM Orders o
-- INNER JOIN OrderDetails od ON o.OrderID = od.OrderID
-- GROUP BY YEAR(o.OrderDate), MONTH(o.OrderDate)
-- ORDER BY 年份, 月份;

-- Q10.4: 客戶價值分析（RFM 基礎）
-- SELECT 
--     c.CompanyName,
--     COUNT(DISTINCT o.OrderID) AS 訂單次數,
--     MAX(o.OrderDate) AS 最後訂購日期,
--     SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS 總消費金額
-- FROM Customers c
-- LEFT JOIN Orders o ON c.CustomerID = o.CustomerID
-- LEFT JOIN OrderDetails od ON o.OrderID = od.OrderID
-- GROUP BY c.CustomerID, c.CompanyName
-- HAVING COUNT(DISTINCT o.OrderID) > 0
-- ORDER BY 總消費金額 DESC
-- LIMIT 20;

-- Q10.5: 產品庫存警報
-- SELECT 
--     p.ProductName,
--     p.UnitsInStock AS 當前庫存,
--     p.ReorderLevel AS 再訂購水平,
--     p.UnitsOnOrder AS 在途訂單,
--     (p.UnitsInStock + p.UnitsOnOrder) AS 可用庫存
-- FROM Products p
-- WHERE p.Discontinued = 0
--   AND p.UnitsInStock < p.ReorderLevel
-- ORDER BY (p.UnitsInStock - p.ReorderLevel);

-- Q10.6: 客戶地理分佈
-- SELECT 
--     Country AS 國家,
--     COUNT(*) AS 客戶數量,
--     COUNT(*) * 100.0 / (SELECT COUNT(*) FROM Customers) AS 百分比
-- FROM Customers
-- GROUP BY Country
-- ORDER BY 客戶數量 DESC;

-- Q10.7: 產品類別分析
-- SELECT 
--     c.CategoryName AS 類別,
--     COUNT(p.ProductID) AS 產品數量,
--     AVG(p.UnitPrice) AS 平均價格,
--     SUM(p.UnitsInStock) AS 總庫存量,
--     SUM(p.UnitsInStock * p.UnitPrice) AS 庫存總值
-- FROM Categories c
-- LEFT JOIN Products p ON c.CategoryID = p.CategoryID
-- GROUP BY c.CategoryID, c.CategoryName
-- ORDER BY 庫存總值 DESC;

-- Q10.8: 延遲出貨分析
-- SELECT 
--     o.OrderID,
--     c.CompanyName,
--     o.OrderDate,
--     o.RequiredDate,
--     o.ShippedDate,
--     DATEDIFF(o.ShippedDate, o.RequiredDate) AS 延遲天數
-- FROM Orders o
-- INNER JOIN Customers c ON o.CustomerID = c.CustomerID
-- WHERE o.ShippedDate IS NOT NULL
--   AND o.ShippedDate > o.RequiredDate
-- ORDER BY 延遲天數 DESC
-- LIMIT 20;

-- Q10.9: 跨年度比較
-- SELECT 
--     p.ProductName,
--     SUM(CASE WHEN YEAR(o.OrderDate) = 2024 THEN od.Quantity ELSE 0 END) AS Sales_2024,
--     SUM(CASE WHEN YEAR(o.OrderDate) = 2025 THEN od.Quantity ELSE 0 END) AS Sales_2025,
--     SUM(CASE WHEN YEAR(o.OrderDate) = 2026 THEN od.Quantity ELSE 0 END) AS Sales_2026
-- FROM Products p
-- LEFT JOIN OrderDetails od ON p.ProductID = od.ProductID
-- LEFT JOIN Orders o ON od.OrderID = o.OrderID
-- GROUP BY p.ProductID, p.ProductName
-- HAVING Sales_2024 > 0 OR Sales_2025 > 0 OR Sales_2026 > 0
-- ORDER BY (Sales_2024 + Sales_2025 + Sales_2026) DESC
-- LIMIT 20;

-- Q10.10: 供應商效能評估
-- SELECT 
--     s.CompanyName AS 供應商,
--     COUNT(DISTINCT p.ProductID) AS 供應產品數,
--     AVG(p.UnitPrice) AS 平均產品價格,
--     SUM(od.Quantity) AS 總銷售量,
--     SUM(od.Quantity * od.UnitPrice * (1 - od.Discount)) AS 總銷售額
-- FROM Suppliers s
-- LEFT JOIN Products p ON s.SupplierID = p.SupplierID
-- LEFT JOIN OrderDetails od ON p.ProductID = od.ProductID
-- GROUP BY s.SupplierID, s.CompanyName
-- ORDER BY 總銷售額 DESC;

-- ========================================
-- 完成
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT 'Practice Complete!' AS '';
SELECT '========================================' AS '';
SELECT '提示：' AS '';
SELECT '  1. 取消註解(移除 --)來執行查詢' AS '';
SELECT '  2. 嘗試修改查詢以學習不同概念' AS '';
SELECT '  3. 組合不同技巧創建自己的查詢' AS '';
SELECT '  4. 使用 EXPLAIN 分析查詢效能' AS '';
SELECT '========================================' AS '';
