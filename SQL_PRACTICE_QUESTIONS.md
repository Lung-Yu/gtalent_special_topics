# SQL 練習題目集

> 💡 **使用方式：** 先嘗試自己寫出 SQL 查詢，完成後可參考 `mysql-init/06-practice-queries.sql` 查看參考答案

---

## 📋 目錄

- [基礎題 (Level 1)](#基礎題-level-1) - 適合初學者
- [中級題 (Level 2)](#中級題-level-2) - 需要理解 JOIN 和聚合
- [進階題 (Level 3)](#進階題-level-3) - 需要複雜查詢和子查詢
- [挑戰題 (Level 4)](#挑戰題-level-4) - 商業分析實戰

---

## 基礎題 (Level 1)

> 適合剛開始學習 SQL 的學生，主要練習 SELECT、WHERE、ORDER BY

### 1-1. 查詢所有產品
**任務：** 顯示 Products 表中所有產品的完整資訊

<details>
<summary>💡 提示</summary>

使用 `SELECT * FROM ...`
</details>

---

### 1-2. 查詢特定欄位
**任務：** 只顯示產品名稱、單價和庫存量

<details>
<summary>💡 提示</summary>

指定欄位名稱：`SELECT column1, column2 FROM ...`
</details>

---

### 1-3. 使用中文別名
**任務：** 查詢產品資訊，將欄位名稱改為中文
- ProductName → 產品名稱
- UnitPrice → 單價
- UnitsInStock → 庫存

<details>
<summary>💡 提示</summary>

使用 `AS` 關鍵字：`SELECT column AS 別名`
</details>

---

### 1-4. 計算庫存總值
**任務：** 顯示每個產品的名稱、單價、庫存量，以及庫存總值（單價 × 庫存量）

<details>
<summary>💡 提示</summary>

可以在 SELECT 中進行計算：`UnitPrice * UnitsInStock`
</details>

---

### 1-5. 去除重複值
**任務：** 列出所有客戶來自哪些不同的國家（不要重複）

<details>
<summary>💡 提示</summary>

使用 `DISTINCT` 關鍵字
</details>

---

### 1-6. 限制結果數量
**任務：** 只顯示前 10 個產品

<details>
<summary>💡 提示</summary>

使用 `LIMIT` 子句
</details>

---

### 1-7. 等於條件
**任務：** 查詢類別 ID 為 1 的所有產品

<details>
<summary>💡 提示</summary>

使用 `WHERE` 子句：`WHERE CategoryID = 1`
</details>

---

### 1-8. 比較運算符
**任務：** 找出單價大於 50 元的產品

<details>
<summary>💡 提示</summary>

使用比較運算符：`>`、`<`、`>=`、`<=`
</details>

---

### 1-9. 範圍查詢
**任務：** 找出單價在 10 到 30 元之間的產品

<details>
<summary>💡 提示</summary>

使用 `BETWEEN ... AND ...`
</details>

---

### 1-10. IN 子句
**任務：** 找出來自德國、法國或英國的客戶

<details>
<summary>💡 提示</summary>

使用 `IN ('value1', 'value2', ...)`
</details>

---

### 1-11. 模糊搜尋 - 開頭
**任務：** 找出產品名稱以 "Ch" 開頭的產品

<details>
<summary>💡 提示</summary>

使用 `LIKE 'Ch%'`，`%` 代表任意字元
</details>

---

### 1-12. 模糊搜尋 - 包含
**任務：** 找出產品名稱中包含 "Chocolate" 的產品

<details>
<summary>💡 提示</summary>

使用 `LIKE '%keyword%'`
</details>

---

### 1-13. NULL 值檢查
**任務：** 找出所有尚未出貨的訂單（ShippedDate 為 NULL）

<details>
<summary>💡 提示</summary>

使用 `IS NULL` 或 `IS NOT NULL`
</details>

---

### 1-14. 邏輯運算 - AND
**任務：** 找出類別 ID 為 1 且單價大於 15 元的產品

<details>
<summary>💡 提示</summary>

使用 `AND` 連接多個條件
</details>

---

### 1-15. 邏輯運算 - OR
**任務：** 找出類別 ID 為 1 或 2 的產品

<details>
<summary>💡 提示</summary>

使用 `OR` 連接條件
</details>

---

### 1-16. 日期查詢
**任務：** 找出 2024 年的所有訂單

<details>
<summary>💡 提示</summary>

使用日期比較：`OrderDate >= '2024-01-01' AND OrderDate < '2025-01-01'`
</details>

---

### 1-17. 升序排序
**任務：** 顯示所有產品，按單價從低到高排序

<details>
<summary>💡 提示</summary>

使用 `ORDER BY column ASC`（ASC 可省略）
</details>

---

### 1-18. 降序排序
**任務：** 找出單價最高的 10 個產品

<details>
<summary>💡 提示</summary>

使用 `ORDER BY column DESC` 配合 `LIMIT`
</details>

---

### 1-19. 多欄位排序
**任務：** 產品按類別升序排列，同類別內按單價降序排列

<details>
<summary>💡 提示</summary>

`ORDER BY column1 ASC, column2 DESC`
</details>

---

### 1-20. 計算欄位排序
**任務：** 產品按庫存總值（單價 × 庫存量）從高到低排序

<details>
<summary>💡 提示</summary>

可以對計算結果排序，也可以使用別名
</details>

---

## 中級題 (Level 2)

> 需要理解聚合函數、GROUP BY 和 JOIN 的概念

### 2-1. 計算總數
**任務：** 計算資料庫中有多少個產品

<details>
<summary>💡 提示</summary>

使用 `COUNT(*)` 函數
</details>

---

### 2-2. 計算不重複的值
**任務：** 計算有多少個不同的國家有客戶

<details>
<summary>💡 提示</summary>

使用 `COUNT(DISTINCT column)`
</details>

---

### 2-3. 求和
**任務：** 計算所有產品的總庫存量

<details>
<summary>💡 提示</summary>

使用 `SUM()` 函數
</details>

---

### 2-4. 求平均值
**任務：** 計算產品的平均單價

<details>
<summary>💡 提示</summary>

使用 `AVG()` 函數
</details>

---

### 2-5. 最大最小值
**任務：** 找出產品的最高價和最低價

<details>
<summary>💡 提示</summary>

使用 `MAX()` 和 `MIN()` 函數
</details>

---

### 2-6. 基本分組
**任務：** 統計每個類別有多少個產品

<details>
<summary>💡 提示</summary>

使用 `GROUP BY CategoryID`
</details>

---

### 2-7. 分組聚合
**任務：** 計算每個類別的產品平均價格

<details>
<summary>💡 提示</summary>

`SELECT CategoryID, AVG(UnitPrice) FROM ... GROUP BY CategoryID`
</details>

---

### 2-8. HAVING 過濾
**任務：** 找出產品數量超過 10 個的類別

<details>
<summary>💡 提示</summary>

使用 `HAVING COUNT(*) > 10`（HAVING 用於過濾分組結果）
</details>

---

### 2-9. WHERE + GROUP BY + HAVING
**任務：** 在未停產的產品中，找出平均價格超過 20 元的類別

<details>
<summary>💡 提示</summary>

WHERE 過濾原始資料，HAVING 過濾分組結果
</details>

---

### 2-10. 國家客戶統計
**任務：** 統計每個國家有多少客戶，按客戶數量降序排列

<details>
<summary>💡 提示</summary>

GROUP BY Country，然後 ORDER BY COUNT(*)
</details>

---

### 2-11. 兩表內聯結
**任務：** 顯示所有產品及其所屬的類別名稱

<details>
<summary>💡 提示</summary>

使用 `INNER JOIN`：`FROM Products p INNER JOIN Categories c ON p.CategoryID = c.CategoryID`
</details>

---

### 2-12. 三表聯結
**任務：** 顯示產品名稱、類別名稱和供應商名稱

<details>
<summary>💡 提示</summary>

需要 JOIN 三個表：Products、Categories、Suppliers
</details>

---

### 2-13. JOIN 帶條件
**任務：** 找出單價超過 30 元的產品及其類別名稱

<details>
<summary>💡 提示</summary>

先 JOIN 再用 WHERE 過濾
</details>

---

### 2-14. LEFT JOIN
**任務：** 列出所有產品和供應商，包括沒有供應商的產品

<details>
<summary>💡 提示</summary>

使用 `LEFT JOIN` 保留左表所有記錄
</details>

---

### 2-15. 找出沒有訂單的客戶
**任務：** 使用 LEFT JOIN 找出從未下過訂單的客戶

<details>
<summary>💡 提示</summary>

LEFT JOIN 後用 `WHERE Orders.OrderID IS NULL`
</details>

---

### 2-16. 自聯結 - 員工層級
**任務：** 顯示每位員工及其直屬主管的名字

<details>
<summary>💡 提示</summary>

將 Employees 表與自己 JOIN：`FROM Employees e LEFT JOIN Employees m ON e.ReportsTo = m.EmployeeID`
</details>

---

### 2-17. 四表聯結
**任務：** 顯示訂單編號、客戶名稱、產品名稱和訂購數量

<details>
<summary>💡 提示</summary>

需要：Orders -> Customers, Orders -> OrderDetails -> Products
</details>

---

### 2-18. 簡單子查詢
**任務：** 找出單價高於平均價格的所有產品

<details>
<summary>💡 提示</summary>

`WHERE UnitPrice > (SELECT AVG(UnitPrice) FROM Products)`
</details>

---

### 2-19. IN 子查詢
**任務：** 找出在 2024 年有下訂單的所有客戶名稱

<details>
<summary>💡 提示</summary>

使用子查詢：`WHERE CustomerID IN (SELECT DISTINCT CustomerID FROM Orders WHERE ...)`
</details>

---

### 2-20. NOT IN 子查詢
**任務：** 找出從未下過訂單的客戶（使用 NOT IN）

<details>
<summary>💡 提示</summary>

`WHERE CustomerID NOT IN (SELECT DISTINCT CustomerID FROM Orders WHERE CustomerID IS NOT NULL)`
</details>

---

## 進階題 (Level 3)

> 需要使用複雜查詢、窗口函數和進階 SQL 技巧

### 3-1. CASE 條件表達式
**任務：** 將產品按價格分級：便宜（< 10）、中等（10-50）、昂貴（> 50）

<details>
<summary>💡 提示</summary>

使用 `CASE WHEN ... THEN ... ELSE ... END`
</details>

---

### 3-2. 日期函數
**任務：** 從訂單日期中提取年、月、日和星期幾

<details>
<summary>💡 提示</summary>

使用 `YEAR()`, `MONTH()`, `DAY()`, `DAYNAME()` 函數
</details>

---

### 3-3. 字串函數
**任務：** 將員工的名字和姓氏合併為全名，並轉換大小寫

<details>
<summary>💡 提示</summary>

使用 `CONCAT()`, `UPPER()`, `LOWER()`, `LENGTH()` 函數
</details>

---

### 3-4. WITH 子句 (CTE)
**任務：** 使用 CTE 找出價格超過 50 的產品及其類別

<details>
<summary>💡 提示</summary>

```sql
WITH ExpensiveProducts AS (
    SELECT ...
)
SELECT ... FROM ExpensiveProducts ...
```
</details>

---

### 3-5. UNION 合併結果
**任務：** 列出所有來自美國的客戶和供應商（合併成一個結果集）

<details>
<summary>💡 提示</summary>

使用 `UNION` 合併兩個 SELECT
</details>

---

### 3-6. 窗口函數 - 排名
**任務：** 為每個類別內的產品按價格排名（使用 ROW_NUMBER）

<details>
<summary>💡 提示</summary>

使用 `ROW_NUMBER() OVER (PARTITION BY CategoryID ORDER BY UnitPrice DESC)`
</details>

---

### 3-7. 窗口函數 - 密集排名
**任務：** 對所有產品按價格進行 DENSE_RANK 排名

<details>
<summary>💡 提示</summary>

`DENSE_RANK() OVER (ORDER BY UnitPrice DESC)`
</details>

---

### 3-8. INSERT 基本操作
**任務：** 新增一個測試類別到 Categories 表

<details>
<summary>💡 提示</summary>

⚠️ 這會修改資料！`INSERT INTO Categories (CategoryName, Description) VALUES (...)`
</details>

---

### 3-9. INSERT 多筆資料
**任務：** 一次新增三個測試類別

<details>
<summary>💡 提示</summary>

`INSERT INTO ... VALUES (...), (...), (...)`
</details>

---

### 3-10. UPDATE 更新
**任務：** 將類別 1 的所有產品價格上漲 10%

<details>
<summary>💡 提示</summary>

⚠️ 這會修改資料！`UPDATE Products SET UnitPrice = UnitPrice * 1.1 WHERE CategoryID = 1`
</details>

---

### 3-11. DELETE 刪除
**任務：** 刪除剛才新增的測試類別

<details>
<summary>💡 提示</summary>

⚠️ 這會刪除資料！`DELETE FROM Categories WHERE CategoryName = 'Test Category'`
</details>

---

### 3-12. 條件更新
**任務：** 將庫存為 0 且無在途訂單的產品標記為停產

<details>
<summary>💡 提示</summary>

`UPDATE Products SET Discontinued = 1 WHERE UnitsInStock = 0 AND UnitsOnOrder = 0`
</details>

---

## 挑戰題 (Level 4)

> 商業分析實戰，需要綜合運用多種 SQL 技巧

### 4-1. 銷售排行榜 🏆
**任務：** 找出銷售額前 10 名的產品，顯示產品名稱、總銷售量和總銷售額

**提示：** 
- 需要 JOIN Products 和 OrderDetails
- 使用 SUM 計算總量和總額
- 記得考慮折扣！銷售額 = Quantity × UnitPrice × (1 - Discount)

---

### 4-2. 員工業績統計 💼
**任務：** 統計每位員工的訂單數量和總銷售額，按銷售額降序排列

**提示：**
- 需要 JOIN Employees、Orders、OrderDetails
- 使用員工的全名（FirstName + LastName）
- 使用 LEFT JOIN 確保沒有訂單的員工也顯示

---

### 4-3. 月度銷售趨勢 📈
**任務：** 按年月統計訂單數量和銷售額，觀察趨勢

**提示：**
- 使用 YEAR() 和 MONTH() 函數
- GROUP BY 年份和月份
- 按時間順序排列

---

### 4-4. 客戶價值分析 (RFM) 💎
**任務：** 分析每個客戶的：
- 訂單次數（Frequency）
- 最後訂購日期（Recency）
- 總消費金額（Monetary）

按總消費金額找出前 20 名 VIP 客戶

**提示：**
- 這是 RFM 分析的基礎
- 需要 JOIN Customers、Orders、OrderDetails
- 使用 MAX(OrderDate) 找最後訂購日期

---

### 4-5. 庫存警報系統 ⚠️
**任務：** 找出需要補貨的產品（未停產且當前庫存低於再訂購水平）

顯示：
- 產品名稱
- 當前庫存
- 再訂購水平
- 在途訂單
- 可用庫存（當前庫存 + 在途訂單）

按缺貨程度排序

---

### 4-6. 客戶地理分佈 🌍
**任務：** 統計每個國家的客戶數量和佔比

**提示：**
- 使用子查詢或窗口函數計算百分比
- 百分比 = 該國客戶數 ÷ 總客戶數 × 100

---

### 4-7. 產品類別績效分析 📊
**任務：** 分析每個產品類別的：
- 產品數量
- 平均價格
- 總庫存量
- 庫存總值（庫存量 × 單價總和）

按庫存總值降序排列

---

### 4-8. 延遲出貨分析 🚚
**任務：** 找出所有延遲出貨的訂單（實際出貨日期晚於要求日期）

顯示：
- 訂單編號
- 客戶名稱
- 訂購日期
- 要求日期
- 實際出貨日期
- 延遲天數

按延遲天數降序排列前 20 筆

---

### 4-9. 跨年度銷售對比 📅
**任務：** 對比每個產品在 2024、2025、2026 年的銷售數量

**提示：**
- 使用 CASE WHEN 配合 SUM
- 或使用 PIVOT 技巧
- 只顯示至少有一年有銷售的產品

---

### 4-10. 供應商績效評估 🏭
**任務：** 評估每個供應商的表現：
- 供應的產品數量
- 產品平均價格
- 總銷售量
- 總銷售額

按總銷售額降序排列

---

### 4-11. 客戶忠誠度分析 ❤️
**任務：** 找出「活躍客戶」（最近一年內有訂單）和「流失客戶」（超過一年沒訂單）

顯示客戶名稱、最後訂購日期、距今天數

**提示：**
- 使用 DATEDIFF 或 DATE_SUB 函數
- 今天是 2026-02-10

---

### 4-12. 產品組合分析 🎯
**任務：** 找出經常一起購買的產品組合（同一訂單中的產品配對）

顯示前 20 組最常見的產品組合及出現次數

**提示：**
- 這是購物籃分析 (Market Basket Analysis)
- 需要自聯結 OrderDetails
- 確保 ProductID_A < ProductID_B 避免重複

---

### 4-13. 員工管理層級圖 👥
**任務：** 顯示完整的員工組織架構（包含層級深度）

使用遞迴或多次自聯結顯示：
- 員工名稱
- 主管名稱
- 層級深度（CEO = 1，VP = 2，其他 = 3）

---

### 4-14. 季度業績目標達成率 🎯
**任務：** 假設每季目標是 50,000 元銷售額，計算每季實際達成率

顯示：
- 年份
- 季度（1-4）
- 實際銷售額
- 目標金額
- 達成率（%）

**提示：**
- 使用 QUARTER() 函數
- 或使用 CASE WHEN 根據月份判斷季度

---

### 4-15. 產品生命週期分析 📉
**任務：** 將產品分類為：
- 新品（上市不到 1 年且銷量好）
- 暢銷品（持續熱賣）
- 衰退品（銷量下降趨勢）
- 滯銷品（幾乎無銷量）

根據訂單數據判斷每個產品的狀態

**提示：**
- 需要比較不同時期的銷量
- 可能需要用到窗口函數或子查詢
- 這是高階商業分析題！

---

## 🎓 評分標準

- **基礎題 (1-1 到 1-20)**: 完成 15 題以上 → 及格
- **中級題 (2-1 到 2-20)**: 完成 12 題以上 → 良好
- **進階題 (3-1 到 3-12)**: 完成 8 題以上 → 優秀
- **挑戰題 (4-1 到 4-15)**: 完成 10 題以上 → 卓越 🏆

---

## 📝 練習建議

1. **按順序練習**：從基礎題開始，逐步提升難度
2. **先思考再查答案**：至少嘗試 10 分鐘再看參考答案
3. **理解而非記憶**：理解每個查詢背後的邏輯
4. **實際執行**：在資料庫中運行，觀察結果
5. **舉一反三**：嘗試修改查詢條件，探索不同場景
6. **使用 EXPLAIN**：分析查詢效能，理解索引的重要性
7. **記錄筆記**：記下不懂的地方和學到的技巧

---

## 🔗 資源連結

- **參考答案**: `mysql-init/06-practice-queries.sql`
- **資料庫說明**: `DATABASE_SETUP.md`
- **索引效能測試**: `mysql-init/05-index-examples.sql`

---

## ⚡ 快速測試你的 SQL

連接資料庫後，可以快速驗證：

```sql
-- 檢查資料是否充足
SELECT 
    'Products' AS TableName, COUNT(*) AS Count FROM Products
UNION ALL SELECT 'Customers', COUNT(*) FROM Customers
UNION ALL SELECT 'Orders', COUNT(*) FROM Orders
UNION ALL SELECT 'OrderDetails', COUNT(*) FROM OrderDetails;

-- 應該看到足夠的資料量才能充分練習！
```

---

**祝學習順利！加油！💪**
