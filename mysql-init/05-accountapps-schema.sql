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

-- 授予權限給 student 使用者
GRANT ALL PRIVILEGES ON accountapps.* TO 'student'@'%';
FLUSH PRIVILEGES;

-- 顯示確認訊息
SELECT 'AccountApps database initialized successfully' AS Status;
