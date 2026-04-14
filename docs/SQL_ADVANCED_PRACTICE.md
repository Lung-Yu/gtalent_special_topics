# SQL 進階練習題目集 🚀

> 💡 **適用對象：** 已完成基礎練習，準備挑戰更複雜的商業分析與資料庫進階功能的學習者

---

## 📋 目錄

- [Level 5 - 專家級分析](#level-5---專家級分析) - 統計分析與進階商業邏輯
- [Level 6 - 資料庫進階功能](#level-6---資料庫進階功能) - Views, Stored Procedures, Triggers
- [Level 7 - 實戰綜合專案](#level-7---實戰綜合專案) - 完整商業案例實作

---

## Level 5 - 專家級分析

> 需要運用統計概念、複雜的窗口函數和多層次的商業邏輯

### 5-1. 遞迴組織架構 - 完整路徑顯示 🌳

**情境：** HR 部門需要完整的員工報告鏈

**任務：** 使用遞迴 CTE 顯示每位員工從自己到 CEO 的完整報告路徑

**預期輸出：**
```
員工名稱 | 層級 | 報告路徑
---------|------|----------
Nancy    | 1    | Nancy
Andrew   | 2    | Andrew -> Nancy
Janet    | 3    | Janet -> Andrew -> Nancy
```

<details>
<summary>💡 提示</summary>

```sql
WITH RECURSIVE OrgHierarchy AS (
    -- 基礎案例：頂層員工
    SELECT ...
    UNION ALL
    -- 遞迴案例：加入下一層
    SELECT ...
)
```
</details>

---

### 5-2. 滑動窗口分析 - 移動平均 📊

**情境：** 財務分析師需要平滑的銷售趨勢線

**任務：** 計算每個月的銷售額及 3 個月移動平均

**要求：**
- 顯示 2024-2026 年每月銷售額
- 計算 3 個月移動平均（當前月 + 前兩個月）
- 計算與移動平均的偏差百分比

<details>
<summary>💡 提示</summary>

使用窗口函數：
```sql
AVG(monthly_sales) OVER (
    ORDER BY year_month 
    ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
)
```
</details>

---

### 5-3. 同期比較分析 - YoY 成長率 📈

**情境：** 管理層要看年度成長趨勢

**任務：** 計算每個產品類別的年度銷售額及 YoY（Year over Year）成長率

**輸出格式：**
```
類別 | 2024銷售額 | 2025銷售額 | 2026銷售額 | 2024-2025成長% | 2025-2026成長%
```

<details>
<summary>💡 提示</summary>

使用 LAG() 窗口函數取得上一年的值：
```sql
LAG(sales_amount) OVER (PARTITION BY category ORDER BY year)
```
</details>

---

### 5-4. 客戶生命週期價值 (CLV) 💎

**情境：** 行銷團隊需要識別最有價值的客戶

**任務：** 計算每個客戶的生命週期價值指標

**計算項目：**
1. 總訂單數
2. 總消費金額
3. 平均訂單價值 (AOV)
4. 首次購買日期
5. 最後購買日期
6. 客戶壽命（天數）
7. 平均購買間隔（天）
8. 預測年度價值 = (365 / 平均購買間隔) × AOV

**輸出：** 前 20 名高價值客戶

<details>
<summary>💡 提示</summary>

需要多個聚合函數配合 DATEDIFF 計算時間間隔
</details>

---

### 5-5. ABC 庫存分類 - 帕累托分析 📦

**情境：** 倉儲經理要實施差異化庫存管理

**任務：** 根據銷售額將產品分為 A/B/C 三類

**分類標準：**
- **A 級產品**：累計貢獻前 80% 銷售額
- **B 級產品**：累計貢獻 80%-95% 銷售額
- **C 級產品**：累計貢獻 95%-100% 銷售額

**輸出：** 每個產品的銷售額、累計佔比、ABC 分類

<details>
<summary>💡 提示</summary>

使用窗口函數計算累計百分比：
```sql
SUM(sales) OVER (ORDER BY sales DESC) / SUM(sales) OVER ()
```
</details>

---

### 5-6. 購物籃分析 - 產品關聯規則 🛒

**情境：** 電商團隊想推薦「常一起購買」的產品

**任務：** 找出最常一起出現在同一訂單中的產品組合

**要求：**
- 找出產品配對（ProductA, ProductB）
- 計算共同出現次數
- 計算支持度 = 共同出現訂單數 / 總訂單數
- 只顯示支持度 > 5% 的組合
- 前 20 名最強關聯

<details>
<summary>💡 提示</summary>

需要自聯結 OrderDetails：
```sql
FROM OrderDetails od1
JOIN OrderDetails od2 
    ON od1.OrderID = od2.OrderID 
    AND od1.ProductID < od2.ProductID
```
</details>

---

### 5-7. 統計異常值偵測 ⚠️

**情境：** 風控團隊需要識別異常訂單

**任務：** 找出統計上的異常訂單

**異常定義：**
- 訂單金額超過平均 + 3倍標準差
- 或訂單金額在前 1% 分位數

**輸出：**
- 訂單編號
- 客戶名稱
- 訂單金額
- Z-Score = (金額 - 平均) / 標準差
- 分位數排名

<details>
<summary>💡 提示</summary>

使用統計函數：
```sql
AVG(), STDDEV(), 
PERCENT_RANK() OVER (ORDER BY amount)
```
</details>

---

### 5-8. 動態樞紐分析 - 產品銷售矩陣 🔄

**情境：** 製作產品與年份的交叉分析表

**任務：** 創建一個樞紐表顯示每個產品在各年度的銷售數量

**輸出格式：**
```
產品名稱 | 2024數量 | 2025數量 | 2026數量 | 總計 | 最佳年份
```

<details>
<summary>💡 提示</summary>

使用 CASE WHEN 配合 SUM：
```sql
SUM(CASE WHEN YEAR(OrderDate) = 2024 THEN Quantity ELSE 0 END) AS "2024數量"
```
</details>

---

### 5-9. 累計銷售計算 - Running Total 📊

**情境：** 追蹤年度累計目標達成情況

**任務：** 顯示 2025 年每月的銷售額和年度累計銷售額

**要求：**
- 每月銷售額
- 年度累計銷售額（從 1 月開始累加）
- 累計目標達成率（假設年度目標 500,000）
- 預測全年銷售額（基於目前進度）

<details>
<summary>💡 提示</summary>

使用窗口函數：
```sql
SUM(monthly_sales) OVER (ORDER BY month ROWS UNBOUNDED PRECEDING)
```
</details>

---

### 5-10. 客戶留存率分析 - Cohort Analysis 👥

**情境：** 衡量客戶黏著度和回購率

**任務：** 按首次購買月份將客戶分群，追蹤各群組的月度留存率

**輸出格式：**
```
首購月份 | 客戶數 | 月1留存 | 月2留存 | 月3留存 | 月4留存 | 月5留存 | 月6留存
2024-01  | 50    | 80%    | 65%    | 55%    | 50%    | 48%    | 45%
```

**留存定義：** 該月有再次下單即算留存

<details>
<summary>💡 提示</summary>

需要多次聯結來計算不同月份的回購：
1. 找出每位客戶的首次購買月份
2. 計算每個後續月份是否有訂單
3. 按 cohort 分組統計留存率
</details>

---

### 5-11. 庫存周轉率分析 🔄

**情境：** 財務部門評估庫存效率和資金佔用

**任務：** 計算每個產品的庫存管理指標

**計算項目：**
1. 年銷售數量（2025年）
2. 平均庫存量（假設當前庫存代表平均）
3. 庫存周轉率 = 年銷量 / 平均庫存
4. 庫存天數 = 365 / 周轉率
5. 評級：
   - 優秀：周轉率 > 12（月均 1 次以上）
   - 良好：周轉率 6-12
   - 注意：周轉率 2-6
   - 滯銷：周轉率 < 2

<details>
<summary>💡 提示</summary>

需要聯結計算銷量，然後用 CASE 分類
</details>

---

### 5-12. 客戶細分進階 - RFM 評分系統 🎯

**情境：** 建立更精細的客戶分群策略

**任務：** 使用 RFM 模型將客戶分為 11 個細分群組

**步驟：**
1. 計算 R (Recency): 最後購買距今天數，分為 1-5 分
2. 計算 F (Frequency): 購買次數，分為 1-5 分
3. 計算 M (Monetary): 總消費金額，分為 1-5 分
4. 使用評分規則將客戶分類：

**客戶分群：**
- **Champions** (冠軍): RFM = 5,5,5 或 5,4,5
- **Loyal Customers** (忠誠): F ≥ 4
- **Potential Loyalist** (潛力): R ≥ 4, F = 2-3
- **New Customers** (新客): R ≥ 4, F = 1
- **Promising** (有潛力): R = 3-4, F = 1
- **Need Attention** (需關注): R = 3, F = 2-3
- **About to Sleep** (即將流失): R = 2-3, F ≤ 2
- **At Risk** (高風險): R ≤ 2, F ≥ 3
- **Can't Lose Them** (重要流失): R ≤ 2, M ≥ 4
- **Hibernating** (休眠): R ≤ 2, F = 1-2
- **Lost** (已流失): R = 1, F = 1

<details>
<summary>💡 提示</summary>

使用 NTILE() 函數分組：
```sql
NTILE(5) OVER (ORDER BY recency_days DESC) AS R_Score
```
然後用複雜的 CASE WHEN 分類
</details>

---

### 5-13. 員工績效與獎金計算 💰

**情境：** 年終績效考核與獎金分配

**任務：** 計算每位員工的績效指標和獎金

**考核指標：**
1. 年度訂單數
2. 年度銷售額
3. 平均訂單價值
4. 部門內排名（RANK）
5. 全公司排名百分位（PERCENT_RANK）

**獎金計算規則：**
- 假設底薪 50,000 元
- 前 10%：獎金 = 底薪 × 3
- 11-30%：獎金 = 底薪 × 2
- 31-60%：獎金 = 底薪 × 1.5
- 61-80%：獎金 = 底薪 × 1
- 後 20%：獎金 = 底薪 × 0.5

<details>
<summary>💡 提示</summary>

使用 PERCENT_RANK() 窗口函數計算百分位
</details>

---

### 5-14. 產品生命週期階段判定 📉

**情境：** 產品經理需要識別每個產品的市場階段

**任務：** 根據銷售趨勢將產品分類

**分類標準：**
- **新品** (New): 上市不到 6 個月，銷量穩定成長
- **成長期** (Growth): 銷量月成長率 > 10%
- **成熟期** (Mature): 銷量穩定，波動 < 10%
- **衰退期** (Decline): 近 3 個月銷量持續下降
- **滯銷** (Stagnant): 近 6 個月月均銷量 < 10 件

**分析時間範圍：** 2025 年全年

<details>
<summary>💡 提示</summary>

需要計算多個時間段的銷量，比較成長率和趨勢
使用子查詢或 CTE 分階段計算
</details>

---

### 5-15. 供應商風險評估矩陣 🏭

**情境：** 採購部門需要評估供應商可靠性

**任務：** 為每個供應商建立風險評分卡

**評估維度：**
1. **產品多樣性**：供應產品數量 (10%)
2. **價格競爭力**：產品平均價格排名 (20%)
3. **庫存穩定性**：缺貨產品佔比 (30%)
4. **產品品質**：停產產品佔比 (20%)
5. **市場表現**：產品總銷售額 (20%)

**評分標準：** 每個維度 0-100 分，計算加權總分

**風險等級：**
- 優質供應商：80-100 分
- 合格供應商：60-79 分
- 觀察名單：40-59 分
- 高風險：< 40 分

<details>
<summary>💡 提示</summary>

需要多層次的計算和評分邏輯
可以使用多個 CTE 分別計算各維度分數
</details>

---

## Level 6 - 資料庫進階功能

> 學習 Views、Stored Procedures、Triggers、Functions 等資料庫物件

### 6-1. 建立銷售摘要視圖 👁️

**情境：** 簡化複雜的銷售查詢

**任務：** 建立一個名為 `v_SalesSummary` 的視圖

**視圖內容：**
- 訂單編號
- 客戶名稱
- 員工姓名
- 訂單日期
- 產品數量（該訂單的產品種類數）
- 訂單總額（考慮折扣）
- 訂單狀態（已出貨/未出貨）

**後續：** 查詢此視圖，找出 2025 年訂單總額前 10 的客戶

```sql
CREATE VIEW v_SalesSummary AS
SELECT 
    ...
FROM Orders o
JOIN ...
```

<details>
<summary>💡 提示</summary>

視圖可以簡化複雜查詢，並可當作表格使用
</details>

---

### 6-2. 建立月度報表視圖 📅

**任務：** 建立 `v_MonthlySalesReport` 視圖

**包含欄位：**
- 年月 (YYYY-MM)
- 訂單數量
- 總銷售額
- 平均訂單價值
- 最大單筆訂單金額
- 活躍客戶數（該月下單的不重複客戶）

---

### 6-3. 建立產品績效視圖 📊

**任務：** 建立 `v_ProductPerformance` 視圖

**包含欄位：**
- 產品 ID 和名稱
- 類別名稱
- 供應商名稱
- 當前庫存
- 總銷售數量
- 總銷售額
- 平均折扣率
- 庫存周轉率
- 績效評級（A/B/C/D）

---

### 6-4. 建立儲存過程 - 產品補貨建議 🔧

**情境：** 自動化補貨決策流程

**任務：** 建立儲存過程 `sp_GetRestockSuggestions`

**輸入參數：**
- `@DaysThreshold INT` - 預計銷售天數（預設 30）

**邏輯：**
1. 計算每個產品的日均銷量（過去 90 天）
2. 計算預計 N 天的需求量
3. 比較當前庫存 + 在途訂單
4. 如果不足，計算建議補貨量

**輸出：** 需要補貨的產品清單及建議數量

```sql
DELIMITER //
CREATE PROCEDURE sp_GetRestockSuggestions(
    IN DaysThreshold INT
)
BEGIN
    -- 你的邏輯
END //
DELIMITER ;
```

---

### 6-5. 建立儲存過程 - 客戶分級更新 🎯

**任務：** 建立 `sp_UpdateCustomerTier` 儲存過程

**功能：** 根據客戶消費總額自動更新客戶等級

**等級分類：**
- VIP：總消費 > 100,000
- Gold：總消費 50,000-100,000
- Silver：總消費 10,000-50,000
- Bronze：總消費 < 10,000

**注意：** 需要先在 Customers 表新增 `CustomerTier VARCHAR(10)` 欄位

---

### 6-6. 建立觸發器 - 庫存自動更新 ⚡

**情境：** 當訂單新增時，自動減少庫存

**任務：** 建立 `trg_UpdateStockAfterOrder` 觸發器

**觸發時機：** OrderDetails 表 INSERT 後

**功能：**
- 自動更新 Products 表的 UnitsInStock
- 如果庫存不足，拋出錯誤並回滾交易

```sql
DELIMITER //
CREATE TRIGGER trg_UpdateStockAfterOrder
AFTER INSERT ON OrderDetails
FOR EACH ROW
BEGIN
    UPDATE Products 
    SET UnitsInStock = UnitsInStock - NEW.Quantity
    WHERE ProductID = NEW.ProductID;
    
    -- 檢查庫存是否為負
    IF (SELECT UnitsInStock FROM Products WHERE ProductID = NEW.ProductID) < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '庫存不足，無法完成訂單';
    END IF;
END //
DELIMITER ;
```

---

### 6-7. 建立自訂函數 - 計算折扣後價格 🔢

**任務：** 建立函數 `fn_CalculateDiscountedPrice`

**輸入：**
- `UnitPrice DECIMAL(10,2)`
- `Quantity INT`
- `Discount DECIMAL(3,2)`

**輸出：** 折扣後總價

```sql
DELIMITER //
CREATE FUNCTION fn_CalculateDiscountedPrice(
    UnitPrice DECIMAL(10,2),
    Quantity INT,
    Discount DECIMAL(3,2)
)
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    RETURN UnitPrice * Quantity * (1 - Discount);
END //
DELIMITER ;
```

**使用範例：**
```sql
SELECT 
    ProductID,
    fn_CalculateDiscountedPrice(UnitPrice, Quantity, Discount) AS TotalPrice
FROM OrderDetails;
```

---

### 6-8. 建立自訂函數 - 客戶風險評分 📊

**任務：** 建立函數 `fn_CustomerRiskScore`

**輸入：** `CustomerID VARCHAR(5)`

**輸出：** 風險評分 0-100（分數越高風險越低）

**計算邏輯：**
- 訂單數量貢獻：30%
- 總消費金額貢獻：40%
- 最近活躍度貢獻：30%

---

### 6-9. 資料完整性檢查 - 孤兒記錄偵測 🔍

**情境：** 定期檢查資料一致性

**任務：** 撰寫查詢找出以下異常：

1. **孤兒產品**：有 CategoryID 但該類別不存在
2. **孤兒訂單明細**：OrderDetails 中的 OrderID 不存在於 Orders
3. **空白訂單**：Orders 中有訂單但沒有對應的 OrderDetails
4. **員工管理異常**：ReportsTo 指向不存在的員工

**輸出：** 彙整報表顯示各類異常的數量和明細

---

### 6-10. 建立稽核日誌系統 📝

**情境：** 追蹤重要資料變更歷史

**任務：**

**步驟 1：** 建立稽核表
```sql
CREATE TABLE AuditLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY,
    TableName VARCHAR(50),
    Operation VARCHAR(10),  -- INSERT, UPDATE, DELETE
    RecordID INT,
    OldValue TEXT,
    NewValue TEXT,
    ChangedBy VARCHAR(50),
    ChangedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**步驟 2：** 建立觸發器記錄 Products 表的價格變更

**步驟 3：** 查詢稽核日誌，找出過去 30 天內價格變動超過 20% 的產品

---

### 6-11. 效能調校實戰 - 慢查詢優化 ⚡

**情境：** 發現以下查詢執行緩慢

```sql
SELECT 
    c.CompanyName,
    COUNT(o.OrderID) AS OrderCount,
    SUM(od.Quantity * od.UnitPrice) AS TotalSales
FROM Customers c
JOIN Orders o ON c.CustomerID = o.CustomerID
JOIN OrderDetails od ON o.OrderID = od.OrderID
WHERE YEAR(o.OrderDate) = 2025
GROUP BY c.CustomerID, c.CompanyName
HAVING TotalSales > 10000
ORDER BY TotalSales DESC;
```

**任務：**
1. 使用 `EXPLAIN` 分析查詢計畫
2. 識別效能瓶頸
3. 建議並建立適當的索引
4. 重寫查詢（如果需要）
5. 比較優化前後的執行時間

<details>
<summary>💡 提示</summary>

可能需要的索引：
- `Orders(OrderDate, CustomerID)`
- `OrderDetails(OrderID)` (通常已存在)
</details>

---

### 6-12. 索引策略設計 🎯

**情境：** 為常見查詢模式設計最佳索引

**任務：** 分析以下查詢需求，設計複合索引策略

**查詢模式：**
1. 按客戶 ID 和日期範圍查詢訂單
2. 按產品類別和價格範圍查詢產品
3. 按員工 ID 和訂單狀態查詢訂單
4. 按國家統計客戶數

**要求：**
- 為每個查詢模式設計最佳索引
- 說明索引欄位順序的選擇理由
- 考慮索引大小和維護成本
- 使用 `EXPLAIN` 驗證索引效果

---

### 6-13. 批次資料更新 - 價格調整 💰

**情境：** 年度價格調整作業

**任務：** 安全地執行批次價格更新

**需求：**
1. 所有類別 1 的產品漲價 5%
2. 所有類別 2 的產品漲價 3%
3. 庫存低於 10 的產品漲價 10%（供需調節）
4. 停產產品不調整

**要求：**
- 使用交易確保 ACID 特性
- 更新前備份當前價格到稽核表
- 如果任何價格超過 100 元，回滾所有變更
- 最後產生變更摘要報表

```sql
START TRANSACTION;

-- 備份
INSERT INTO PriceHistory SELECT ...

-- 更新
UPDATE Products SET ...

-- 驗證
IF (條件) THEN
    ROLLBACK;
ELSE
    COMMIT;
END IF;
```

---

### 6-14. 建立資料品質監控儀表板 📊

**任務：** 建立一組查詢/視圖用於資料品質監控

**監控項目：**

1. **完整性檢查**
   - 缺少必要資料的記錄數（NULL 值）
   - 外鍵參考完整性

2. **合理性檢查**
   - 價格為負數或 0 的產品
   - 未來日期的訂單
   - 數量為 0 的訂單明細

3. **一致性檢查**
   - 訂單總額與明細加總不一致
   - 已出貨但無出貨日期

4. **重複性檢查**
   - 完全重複的訂單明細
   - 疑似重複的客戶記錄

**輸出：** 建立 `v_DataQualityDashboard` 視圖彙總所有檢查結果

---

### 6-15. 建立測試資料產生器 🔧

**任務：** 建立儲存過程 `sp_GenerateTestOrders`

**功能：** 自動產生測試訂單資料用於效能測試

**輸入參數：**
- `@NumOrders INT` - 要產生的訂單數量
- `@StartDate DATE` - 起始日期
- `@EndDate DATE` - 結束日期

**邏輯：**
- 隨機選擇客戶
- 隨機選擇員工
- 隨機選擇 1-10 個產品
- 隨機數量 1-50
- 隨機折扣 0-0.25
- 隨機出貨日期（訂單日期後 1-7 天）

---

## Level 7 - 實戰綜合專案

> 完整的商業案例，需要綜合運用多種技術

### 專案 1：銷售分析儀表板 📊

**背景：** 為管理層建立完整的銷售分析系統

**任務清單：**

#### 1.1 建立基礎視圖（4個）
- `v_DailySales` - 每日銷售摘要
- `v_ProductPerformance` - 產品績效
- `v_CustomerSegments` - 客戶分群
- `v_EmployeePerformance` - 員工績效

#### 1.2 建立分析查詢（5個）
- 銷售趨勢分析：月度、季度、年度
- TOP 10 排行榜：產品、客戶、員工
- 同期比較：YoY, MoM, QoQ
- 地理分佈分析：按國家、城市
- 產品組合分析：交叉銷售機會

#### 1.3 建立自動化報表儲存過程（3個）
- `sp_GenerateMonthlyReport` - 月度業績報表
- `sp_GenerateExecutiveSummary` - 執行摘要
- `sp_GenerateAlerts` - 異常警報  

#### 1.4 建立 KPI 計算函數（3個）
- `fn_CalculateGrowthRate` - 成長率
- `fn_CalculateMarketShare` - 市場佔有率
- `fn_CalculateForecast` - 簡單預測

**交付標準：**
- 所有物件建立成功
- 提供使用文件
- 執行效能測試
- 輸出範例報表

---

### 專案 2：客戶關係管理系統 (CRM) 👥

**背景：** 建立完整的客戶分析與管理框架

**任務清單：**

#### 2.1 客戶價值分析
- RFM 評分與分群（11 個群組）
- 客戶生命週期價值（CLV）計算
- 客戶流失風險預測
- VIP 客戶識別

#### 2.2 客戶行為分析
- 購買頻率分析
- 產品偏好分析
- 購買季節性分析
- 購物籃分析（產品關聯）

#### 2.3 行銷活動支援
- 目標客群選擇
- 個性化產品推薦
- 再行銷名單產生
- 活動效果追蹤

#### 2.4 自動化功能
- 客戶等級自動更新（觸發器）
- 流失預警通知（儲存過程）
- 客戶 360 度視圖（視圖）

**交付標準：**
- 完整的 CRM 資料模型
- 至少 10 個分析查詢
- 3 個儲存過程
- 2 個觸發器
- 5 個視圖

---

### 專案 3：庫存優化系統 📦

**背景：** 建立智能庫存管理與補貨建議系統

**任務清單：**

#### 3.1 庫存分析
- ABC 分類（帕累托分析）
- 庫存周轉率分析
- 滯銷品識別
- 安全庫存計算

#### 3.2 補貨建議系統
- 根據銷售趨勢預測需求
- 考慮前置時間
- 計算經濟訂購量（EOQ）
- 產生補貨建議清單

#### 3.3 成本分析
- 庫存持有成本
- 缺貨成本估算
- 庫存總值追蹤
- 資金佔用分析

#### 3.4 自動化與警報
- 低庫存警報（觸發器）
- 滯銷品警報（儲存過程）
- 庫存異常偵測
- 定期補貨建議（排程作業）

**交付標準：**
- 補貨邏輯儲存過程
- 庫存儀表板視圖
- 每日庫存報表
- 警報系統

---

### 專案 4：供應鏈效能分析 🚚

**背景：** 評估和優化供應鏈各環節的表現

**任務清單：**

#### 4.1 供應商績效評估
- 多維度評分系統
- 準時交貨率
- 產品品質指標
- 價格競爭力分析
- 供應商風險評估

#### 4.2 物流效能分析
- 訂單處理時間分析
- 出貨準時率
- 延遲出貨成本計算
- 運輸商績效比較

#### 4.3 訂單履行分析
- 訂單完成率
- 平均處理時間
- 訂單異常分析
- 客戶滿意度指標

#### 4.4 優化建議
- 識別瓶頸環節
- 成本節省機會
- 流程改善建議

**交付標準：**
- 供應商評分卡
- 物流 KPI 儀表板
- 異常訂單報表
- 優化建議報告

---

### 專案 5：資料品質監控與治理 🔍

**背景：** 建立自動化的資料品質監控系統

**任務清單：**

#### 5.1 資料完整性檢查
- 必填欄位檢查
- 外鍵參考完整性
- 孤兒記錄偵測
- 空值分析

#### 5.2 資料合理性檢查
- 數值範圍檢查
- 日期邏輯檢查
- 業務規則驗證
- 異常值偵測

#### 5.3 資料一致性檢查
- 跨表一致性驗證
- 計算欄位核對
- 狀態轉換檢查
- 重複記錄偵測

#### 5.4 自動化監控
- 定期品質檢查（排程）
- 品質分數計算
- 異常警報通知
- 品質趨勢追蹤

**交付標準：**
- 資料品質檢查套件（10+ 檢查）
- 品質儀表板視圖
- 自動化檢查儲存過程
- 警報機制

---

### 專案 6：效能優化專案 ⚡

**背景：** 系統效能調校與最佳化

**任務清單：**

#### 6.1 效能診斷
- 識別慢查詢（查詢日誌分析）
- 執行計畫分析
- 資源使用分析
- 瓶頸識別

#### 6.2 索引優化
- 分析查詢模式
- 設計索引策略
- 建立/修改索引
- 效果驗證

#### 6.3 查詢重寫
- 改寫複雜查詢
- 減少子查詢
- 優化 JOIN 順序
- 使用適當的聚合

#### 6.4 效能測試
- 建立測試資料集
- 壓力測試
- 效能基準測試
- 優化前後比較

**交付標準：**
- 效能診斷報告
- 索引優化建議
- 查詢優化案例（5+）
- 效能提升證明（前後比較）

---

## 🎓 學習路徑建議

### 初學者 → 中級
1. 完成基礎練習（Level 1-2）
2. 嘗試 Level 5 的前 5 題
3. 學習建立簡單的視圖（6-1 到 6-3）

### 中級 → 高級
1. 完成 Level 3-4 所有題目
2. 完成 Level 5 的統計分析題
3. 學習建立儲存過程和觸發器（6-4 到 6-6）
4. 嘗試一個小型專案（專案 1 或 3）

### 高級 → 專家
1. 完成所有 Level 5-6 題目
2. 完成至少 3 個完整專案
3. 深入研究效能優化
4. 建立自己的資料分析框架

---

## 📊 評分標準

- **Level 5 完成 8 題以上**：專家級 SQL 能力 ⭐⭐⭐
- **Level 6 完成 10 題以上**：資料庫管理能力 ⭐⭐⭐⭐
- **完成 2 個以上專案**：實戰能力認證 ⭐⭐⭐⭐⭐
- **完成 4 個以上專案**：資深資料分析師/DBA 水準 🏆

---

## 💡 重要提醒

1. **安全性**：
   - 修改資料前務必備份
   - 使用交易確保資料一致性
   - 測試環境先驗證再上正式環境

2. **效能考量**：
   - 大量資料操作時注意效能
   - 適當使用索引
   - 避免全表掃描

3. **最佳實踐**：
   - 程式碼加上註解
   - 使用有意義的命名
   - 遵循 SQL 編碼規範
   - 錯誤處理要完善

4. **持續學習**：
   - 研究執行計畫
   - 理解資料庫內部機制
   - 關注業界最佳實踐
   - 參與社群討論

---

## 🔗 相關資源

- **基礎練習**：[SQL_PRACTICE_QUESTIONS.md](SQL_PRACTICE_QUESTIONS.md)
- **資料庫設定**：`DATABASE_SETUP.md`
- **ER 圖**：`docs/northwind-er-diagram.md`
- **效能測試範例**：`mysql-init/05-index-examples.sql`
- **⚡ 效能優化教學**：[SQL_PERFORMANCE_TUTORIAL.md](SQL_PERFORMANCE_TUTORIAL.md) - 查詢效能優化實戰，手把手教學

---

**祝你在 SQL 進階之路上不斷精進！💪🚀**

*最後更新：2026-03-15*
