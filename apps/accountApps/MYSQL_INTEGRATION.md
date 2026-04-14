# AccountApps MySQL 資料庫驗證 - 使用指南

## 📋 概述

AccountApps 已成功整合 MySQL 資料庫驗證系統，取代原本的 CSV 檔案驗證。

## ✅ 已完成的更改

### 1. 資料庫基礎設施
- ✅ 建立 `accountapps` 資料庫
- ✅ 建立 `users` 資料表（username 為主鍵）
- ✅ 密碼使用 Caesar Cipher 加密儲存（與原 CSV 邏輯一致）

### 2. 新增 Maven 依賴
- ✅ `mysql-connector-j:8.0.33` - MySQL JDBC 驅動
- ✅ `HikariCP:5.0.1` - 高效能連接池

### 3. 新增類別
- ✅ `DatabaseConnectionFactory` - 管理資料庫連接池
- ✅ `MySQLUserRepository` - MySQL 實作的使用者儲存庫
- ✅ `DatabaseConnectionException` - 資料庫例外處理

### 4. 介面強化
- ✅ `UserRepository` 新增 `findByUsername(String)` 方法
- ✅ 所有實作類別（InMemory, InCSV, MySQL）都已實作此方法

### 5. 性能優化
- ✅ `LoginController.authenticate()` 從 `findAll()` 改為 `findByUsername()`
- ✅ 減少不必要的資料載入，提升查詢效率

## 🚀 使用步驟

### Step 1: 啟動資料庫

```bash
cd /Users/tygr/Desktop/projects/gtalent_special_topics
docker-compose up -d
```

### Step 2: 確認資料庫已建立

```bash
docker exec -it northwind_mysql mysql -uroot -p123456 -e "SHOW DATABASES;"
```

應該看到 `accountapps` 在列表中。

### Step 3: 檢查測試用戶

```bash
docker exec -it northwind_mysql mysql -uroot -p123456 accountapps \
  -e "SELECT username, created_at FROM users;"
```

**預設測試帳號**：
| 帳號 | 密碼 | 密碼（加密） |
|------|------|-------------|
| admin | admin | dgplq |
| user | user | xvhu |
| test | test | whvw |

### Step 4: 執行應用程式

```bash
cd accountApps
mvn exec:java -Dexec.mainClass="com.example.App"
```

### Step 5: 測試登入

1. 輸入帳號：`admin`
2. 輸入密碼：`admin`
3. 應該成功登入並進入主選單

## 🔧 手動新增使用者（可選）

### 方法 1: 直接執行 SQL

```bash
docker exec -it northwind_mysql mysql -uroot -p123456 accountapps
```

在 MySQL 命令列中：

```sql
-- 新增使用者（密碼需先用 Caesar Cipher 加密）
INSERT INTO users (username, password) VALUES ('newuser', 'qhzxvhu');
-- 'newuser' 的密碼是 'newpass'，加密後為 'qhzxvhu'

-- 查看所有使用者
SELECT * FROM users;
```

### 方法 2: 使用應用程式（若有註冊功能）

如果應用程式有使用者註冊功能，透過 `UserRepository.save(user)` 方法會自動加密密碼並儲存到資料庫。

## 🔐 密碼加密說明

系統使用 **Caesar Cipher（凱薩加密）** 處理密碼：
- **位移量**: 3
- **範例**: `admin` → `dgplq`
- **反向**: `dgplq` → `admin`

⚠️ **注意**: Caesar Cipher 僅供學習用途，生產環境應使用 BCrypt、PBKDF2 或 Argon2。

## 📊 資料表結構

```sql
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 🔄 架構說明

### Repository Pattern 實作

```
UserRepository (interface)
    ├── InMemoryUserRepository (記憶體)
    ├── InCSVUserRepository (CSV 檔案)
    └── MySQLUserRepository (MySQL 資料庫) ← 目前使用
```

### 依賴注入流程

```java
// App.java
UserRepository userRepository = new MySQLUserRepository();
LoginController loginController = new LoginController(scanner, userRepository);
```

### 資料庫連接池

- **連接池**: HikariCP
- **最大連接數**: 10
- **最小閒置連接數**: 2
- **連接逾時**: 30 秒
- **連接最大生命週期**: 30 分鐘

## 🐛 疑難排解

### 問題 1: 無法連接資料庫

```bash
# 檢查容器狀態
docker ps | grep northwind_mysql

# 檢查健康狀態
docker inspect northwind_mysql | grep -A 10 Health

# 查看錯誤日誌
docker-compose logs db
```

### 問題 2: accountapps 資料庫不存在

```bash
# 手動執行初始化腳本
docker exec -i northwind_mysql mysql -uroot -p123456 < mysql-init/05-accountapps-schema.sql
```

### 問題 3: 登入失敗

```bash
# 確認使用者是否存在
docker exec -it northwind_mysql mysql -uroot -p123456 accountapps \
  -e "SELECT username, password, created_at FROM users;"

# 驗證密碼加密是否正確（應該看到 'dgplq' 而非  'admin'）
```

### 問題 4: 編譯錯誤

```bash
cd accountApps
mvn clean install -U  # 強制更新依賴
```

## 🧪 驗證測試

### 測試 1: 資料庫連接

```bash
docker exec -it northwind_mysql mysql -ustudent -pstudent123 accountapps \
  -e "SELECT COUNT(*) as user_count FROM users;"
```

### 測試 2: 手動驗證加密

啟動 MySQL CLI 並執行：

```sql
USE accountapps;

-- 檢查原始加密密碼
SELECT username, password FROM users;

-- 預期結果：
-- admin | dgplq
-- user  | xvhu
-- test  | whvw
```

## 📝 程式碼結構

### 關鍵修改檔案

1. **[mysql-init/05-accountapps-schema.sql](../mysql-init/05-accountapps-schema.sql)** - 資料庫 Schema
2. **[pom.xml](pom.xml)** - MySQL 依賴
3. **[DatabaseConnectionFactory.java](src/main/java/com/example/infrastructure/util/DatabaseConnectionFactory.java)** - 連接池管理
4. **[MySQLUserRepository.java](src/main/java/com/example/infrastructure/persistence/MySQLUserRepository.java)** - MySQL 實作
5. **[UserRepository.java](src/main/java/com/example/domain/repository/UserRepository.java)** - 新增 findByUsername 方法
6. **[App.java](src/main/java/com/example/App.java)** - 使用 MySQLUserRepository
7. **[LoginController.java](src/main/java/com/example/presentation/LoginController.java)** - 優化認證邏輯

## 🎯 功能特點

- ✅ 使用獨立的 `accountapps` 資料庫
- ✅ HikariCP 高效連接池管理
- ✅ 保持 Caesar Cipher 加密相容性
- ✅ 遵循 Repository Pattern 設計模式
- ✅ PreparedStatement 防止 SQL Injection
- ✅ 自動資源管理（try-with-resources）
- ✅ 優化的查詢性能（findByUsername）
- ✅ 完整的例外處理

## 🔄 切換回 CSV 驗證（若需要）

只需在 [App.java](src/main/java/com/example/App.java) 中修改：

```java
// MySQL 驗證（當前）
this.userRepository = new MySQLUserRepository();

// 切換回 CSV 驗證
this.userRepository = new InCSVUserRepository();
```

這展現了 Repository Pattern 的強大之處！
