#!/bin/bash

# AccountApps MySQL 資料庫驗證 - 快速測試腳本

echo "========================================="
echo "AccountApps MySQL 整合測試"
echo "========================================="
echo ""

# 1. 檢查 Docker 容器狀態
echo "📦 檢查 Docker 容器..."
if docker ps | grep -q northwind_mysql; then
    echo "✓ MySQL 容器運行中"
else
    echo "✗ MySQL 容器未運行，正在啟動..."
    docker-compose up -d
    sleep 15
fi
echo ""

# 2. 檢查資料庫
echo "🗄️  檢查 accountapps 資料庫..."
DB_EXISTS=$(docker exec northwind_mysql mysql -uroot -p123456 -e "SHOW DATABASES LIKE 'accountapps';" 2>/dev/null | grep -c accountapps)
if [ "$DB_EXISTS" -eq "0" ]; then
    echo "✗ accountapps 資料庫不存在，正在建立..."
    docker exec -i northwind_mysql mysql -uroot -p123456 < mysql-init/05-accountapps-schema.sql
    echo "✓ 資料庫已建立"
else
    echo "✓ accountapps 資料庫已存在"
fi
echo ""

# 3. 檢查 users 表
echo "👥 檢查 users 資料表..."
docker exec northwind_mysql mysql -uroot -p123456 accountapps -e "DESCRIBE users;" 2>/dev/null
echo ""

# 4. 顯示現有使用者
echo "📋 現有使用者列表："
docker exec northwind_mysql mysql -uroot -p123456 accountapps -e "SELECT username, LEFT(password, 10) as password_preview, created_at FROM users;" 2>/dev/null
echo ""

# 5. 測試連線
echo "🔗 測試資料庫連線..."
docker exec northwind_mysql mysql -ustudent -pstudent123 -e "SELECT 'Connection successful' AS Status;" 2>/dev/null
echo ""

# 6. 編譯專案
echo "🔨 編譯 AccountApps 專案..."
cd accountApps
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✓ 編譯成功"
else
    echo "✗ 編譯失敗"
    exit 1
fi
echo ""

# 7. 提示執行方式
echo "========================================="
echo "🎉 設定完成！"
echo "========================================="
echo ""
echo "執行應用程式："
echo "  mvn exec:java -Dexec.mainClass=\"com.example.App\""
echo ""
echo "測試帳號："
echo "  帳號: admin  密碼: admin"
echo "  帳號: user   密碼: user"
echo "  帳號: test   密碼: test"
echo ""
echo "新增使用者（資料庫命令）："
echo "  docker exec -it northwind_mysql mysql -ustudent -pstudent123 accountapps"
echo "  > INSERT INTO users (username, password) VALUES ('帳號', '加密後密碼');"
echo ""
