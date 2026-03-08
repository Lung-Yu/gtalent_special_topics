-- ========================================
-- 產生大規模測試資料（100,000+ 筆支出記錄）
-- 用於測試 Cursor-based Pagination 的效能
-- ========================================

USE accountapps;

-- 設定變數
SET @num_records = 100000;
SET @start_date = DATE_SUB(CURDATE(), INTERVAL 730 DAY); -- 2 年前
SET @test_username = 'perftest_user';

-- 建立測試使用者
INSERT IGNORE INTO users (username, password) 
VALUES (@test_username, 'test_password_encrypted');

-- 建立額外的測試使用者（用於多使用者場景）
INSERT IGNORE INTO users (username, password) 
VALUES 
    ('perftest_user2', 'test_password_encrypted'),
    ('perftest_user3', 'test_password_encrypted');

-- 產生大量支出記錄
-- 使用 MySQL 的遞迴 CTE（Common Table Expression）產生序列
DROP PROCEDURE IF EXISTS generate_expenditure_data;

DELIMITER $$

CREATE PROCEDURE generate_expenditure_data()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE batch_size INT DEFAULT 1000;
    DECLARE total_batches INT;
    DECLARE random_days INT;
    DECLARE random_amount INT;
    DECLARE random_payment INT;
    DECLARE random_name_idx INT;
    DECLARE expenditure_name VARCHAR(255);
    DECLARE payment_method VARCHAR(20);
    DECLARE expenditure_date DATE;
    DECLARE inserted_id BIGINT;
    DECLARE random_category_count INT;
    DECLARE j INT;
    
    SET total_batches = CEIL(@num_records / batch_size);
    
    -- 支出名稱列表
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_names (
        idx INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(255)
    );
    
    INSERT INTO temp_names (name) VALUES
        ('早餐'), ('午餐'), ('晚餐'), ('咖啡'), ('飲料'),
        ('交通費'), ('計程車'), ('捷運'), ('油費'), ('停車費'),
        ('購物'), ('衣服'), ('鞋子'), ('包包'), ('配件'),
        ('娛樂'), ('電影'), ('KTV'), ('遊戲'), ('書籍'),
        ('水電費'), ('電話費'), ('網路費'), ('房租'), ('保險'),
        ('醫療'), ('藥品'), ('健身'), ('美容'), ('理髮'),
        ('旅遊'), ('住宿'), ('機票'), ('伴手禮'), ('紀念品'),
        ('學費'), ('課程'), ('文具'), ('電腦'), ('軟體'),
        ('禮物'), ('紅包'), ('捐款'), ('維修'), ('清潔'),
        ('寵物'), ('零食'), ('水果'), ('蔬菜'), ('肉類');
    
    -- 分類列表
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_categories (
        idx INT PRIMARY KEY AUTO_INCREMENT,
        category VARCHAR(100)
    );
    
    INSERT INTO temp_categories (category) VALUES
        ('飲食'), ('交通'), ('購物'), ('娛樂'), ('居家'),
        ('醫療'), ('教育'), ('旅遊'), ('其他');
    
    SET autocommit = 0;
    
    -- 批次插入資料
    WHILE i < @num_records DO
        -- 隨機產生日期（過去 2 年內）
        SET random_days = FLOOR(RAND() * 730);
        SET expenditure_date = DATE_ADD(@start_date, INTERVAL random_days DAY);
        
        -- 隨機產生金額（10 - 10000）
        SET random_amount = FLOOR(10 + RAND() * 9990);
        
        -- 隨機選擇支付方式
        SET random_payment = FLOOR(RAND() * 3);
        SET payment_method = CASE random_payment
            WHEN 0 THEN 'LinePay'
            WHEN 1 THEN 'AppPay'
            ELSE 'GooglePay'
        END;
        
        -- 隨機選擇支出名稱
        SET random_name_idx = FLOOR(1 + RAND() * 50);
        SELECT name INTO expenditure_name FROM temp_names WHERE idx = random_name_idx LIMIT 1;
        
        -- 插入支出記錄（80% 給主要測試使用者，20% 分散給其他使用者）
        INSERT INTO expenditure_records (username, name, money, payment_method, date)
        VALUES (
            CASE WHEN RAND() < 0.8 THEN @test_username 
                 WHEN RAND() < 0.5 THEN 'perftest_user2'
                 ELSE 'perftest_user3' 
            END,
            expenditure_name,
            random_amount,
            payment_method,
            expenditure_date
        );
        
        SET inserted_id = LAST_INSERT_ID();
        
        -- 為每筆支出隨機添加 1-3 個分類
        SET random_category_count = FLOOR(1 + RAND() * 3);
        SET j = 0;
        
        WHILE j < random_category_count DO
            INSERT IGNORE INTO expenditure_categories (expenditure_id, category_name)
            SELECT inserted_id, category 
            FROM temp_categories 
            ORDER BY RAND() 
            LIMIT 1;
            
            SET j = j + 1;
        END WHILE;
        
        SET i = i + 1;
        
        -- 每 1000 筆提交一次
        IF i % batch_size = 0 THEN
            COMMIT;
            SELECT CONCAT('已產生 ', i, ' / ', @num_records, ' 筆記錄 (', ROUND(i * 100.0 / @num_records, 2), '%)') AS Progress;
        END IF;
    END WHILE;
    
    COMMIT;
    SET autocommit = 1;
    
    -- 清理臨時表
    DROP TEMPORARY TABLE IF EXISTS temp_names;
    DROP TEMPORARY TABLE IF EXISTS temp_categories;
    
    SELECT 
        '測試資料產生完成' AS Status,
        COUNT(*) AS TotalRecords,
        COUNT(DISTINCT username) AS TotalUsers,
        MIN(date) AS EarliestDate,
        MAX(date) AS LatestDate
    FROM expenditure_records;
    
END$$

DELIMITER ;

-- 執行資料產生程序
CALL generate_expenditure_data();

-- 分析資料分佈
SELECT 
    '資料分佈統計' AS Report,
    username AS User,
    COUNT(*) AS RecordCount,
    MIN(date) AS FirstRecord,
    MAX(date) AS LastRecord,
    ROUND(AVG(money), 2) AS AvgAmount
FROM expenditure_records
GROUP BY username
ORDER BY RecordCount DESC;

-- 檢查索引狀態
SHOW INDEX FROM expenditure_records;

-- 優化表（更新統計資訊）
ANALYZE TABLE expenditure_records;
ANALYZE TABLE expenditure_categories;

SELECT 'Large test data generation completed successfully!' AS Status;
