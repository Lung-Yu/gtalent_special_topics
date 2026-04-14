-- ========================================
-- AccountApps 資料庫初始化腳本
-- ========================================

-- 建立 accountapps 資料庫
CREATE DATABASE IF NOT EXISTS accountapps 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 切換到 accountapps 資料庫
USE accountapps;

-- 建立 users 資料表
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY COMMENT '使用者名稱（主鍵）',
    password VARCHAR(255) NOT NULL COMMENT '密碼（Caesar Cipher 加密）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者資料表';

-- 建立索引（雖然 username 已是主鍵，但明確說明用途）
-- CREATE INDEX idx_username ON users(username);  -- 不需要，因為已是主鍵

-- 建立 expenditure_records 資料表（支出記錄）
CREATE TABLE IF NOT EXISTS expenditure_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支出記錄 ID（主鍵）',
    username VARCHAR(50) NOT NULL COMMENT '使用者名稱（外鍵）',
    name VARCHAR(255) NOT NULL COMMENT '支出名稱',
    money INT NOT NULL COMMENT '金額',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式（LinePay/AppPay/GooglePay）',
    date DATE NOT NULL COMMENT '支出日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_username_date (username, date) COMMENT '使用者和日期複合索引',
    INDEX idx_date (date) COMMENT '日期索引',
    INDEX idx_username_date_id_desc (username, date DESC, id DESC) COMMENT 'Cursor-based pagination 索引（降序）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支出記錄資料表';

-- 建立 expenditure_categories 資料表（支出分類關聯，多對多關係）
CREATE TABLE IF NOT EXISTS expenditure_categories (
    expenditure_id BIGINT NOT NULL COMMENT '支出記錄 ID（外鍵）',
    category_name VARCHAR(100) NOT NULL COMMENT '分類名稱',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (expenditure_id, category_name),
    FOREIGN KEY (expenditure_id) REFERENCES expenditure_records(id) ON DELETE CASCADE,
    INDEX idx_expenditure_id (expenditure_id) COMMENT '支出記錄索引',
    INDEX idx_category_name (category_name) COMMENT '分類名稱索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支出分類關聯表';

-- 授予權限給 student 使用者
GRANT ALL PRIVILEGES ON accountapps.* TO 'student'@'%';
FLUSH PRIVILEGES;

-- 顯示確認訊息
SELECT 'AccountApps database initialized successfully' AS Status;
