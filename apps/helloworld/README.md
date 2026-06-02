# Helloworld — Spring Boot 範例應用

## 技術棧

| 項目 | 版本 |
|------|------|
| Java | 21 |
| Spring Boot | 3.5.x |
| MySQL | 8.0 |
| MinIO | latest |
| 認證 | JWT (JJWT 0.12.6) |

---

## 快速啟動（本機開發）

### 前置需求

- JDK 21+
- Maven 3.9+
- Docker + Docker Compose

### 1. 啟動基礎設施（MySQL + MinIO）

```bash
cd apps/helloworld
docker compose up -d mysql minio createbuckets
```

等待所有容器健康（約 30 秒）：

```bash
docker compose ps
```

預期輸出：

```
NAME                    STATUS
helloworld-mysql        Up (healthy)
helloworld-minio        Up (healthy)
helloworld-minio-init   Exited (0)   ← 正常，建立完 bucket 後即退出
```

### 2. 啟動 Spring Boot 應用

```bash
mvn spring-boot:run -f apps/helloworld/pom.xml
```

應用預設監聽 **http://localhost:8888**

---

## 服務連線資訊

### MySQL

| 項目 | 值 |
|------|----|
| Host | `localhost:3306` |
| Database | `helloworld` |
| Username | `helloworld` |
| Password | `helloworld123` |
| Root Password | `rootpassword` |

### MinIO

| 項目 | 值 |
|------|----|
| S3 API Endpoint | `http://localhost:9000` |
| Web Console | `http://localhost:9001` |
| Access Key | `minioadmin` |
| Secret Key | `minioadmin` |
| Bucket | `uploads` |

> **備注**：`createbuckets` container 會在 MinIO 啟動健康後自動建立 `uploads` bucket。

---

## 儲存機制

### 切換模式

透過 `application.properties` 的 `storage.provider` 控制：

```properties
# MinIO（預設）
storage.provider=minio

# 本地磁碟（fallback）
storage.provider=filesystem
```

### MinIO 模式說明

- **物件 key 格式**：`<sha256hex>.<副檔名>`（例：`c450ef00...86f.txt`）
- **去重**：相同 SHA-256 的檔案只存一份物件，多筆 `FileMetadata` 共享同一個 `StoredFile` 記錄
- **Chunked upload 暫存**：上傳中的分塊先寫入本地 `upload-dir/staging/<uploadId>`，`complete` 時再串流推送至 MinIO

### 環境變數覆蓋（Docker / 生產環境）

```bash
MINIO_ENDPOINT=http://your-minio:9000
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET_NAME=uploads
```

---

## 檔案上傳 API

所有上傳端點均需 JWT 認證：`Authorization: Bearer <token>`

### 取得 JWT Token

```bash
curl -X POST http://localhost:8888/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

回應：

```json
{ "token": "eyJhbGci..." }
```

---

### Phase 1 — 單次上傳（最大 100 GB）

```bash
curl -X POST http://localhost:8888/v1/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/your/file.txt"
```

**成功回應（HTTP 200）**：

```json
{
  "id": 1,
  "storedFile": {
    "id": 1,
    "hash": "c450ef00...",
    "path": "c450ef00....txt",
    "createdAt": "2026-05-31T12:03:14"
  },
  "originalName": "file.txt",
  "contentType": "text/plain",
  "fileSize": 51,
  "uploadedAt": "2026-05-31T12:03:14"
}
```

---

### Phase 2 — 分塊可續傳上傳

適用於大型檔案，支援斷點續傳。

#### Step 1：建立 Upload Session

```bash
curl -X POST http://localhost:8888/v1/upload/init \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "originalName": "bigfile.zip",
    "contentType": "application/zip",
    "totalSize": 1073741824
  }'
```

**成功回應（HTTP 201）**：

```json
{
  "uploadId": "7a6736a3-6d70-483d-8316-d3ba3c0cebb4",
  "status": "PENDING",
  "totalSize": 1073741824,
  "expiredAt": "2026-06-01T12:00:00"
}
```

#### Step 2：上傳每個分塊

```bash
curl -X PUT "http://localhost:8888/v1/upload/<uploadId>/chunk?offset=0" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @chunk_part_0
```

- `offset`：此分塊在整體檔案中的 byte 起始位置
- 各分塊可亂序上傳，支援並行

#### Step 3：完成上傳

```bash
curl -X POST "http://localhost:8888/v1/upload/<uploadId>/complete" \
  -H "Authorization: Bearer <token>"
```

**成功回應（HTTP 200）**：與 Phase 1 相同格式。

---

## 常用 Docker 指令

```bash
# 查看所有容器狀態
docker compose ps

# 查看 MinIO logs
docker compose logs -f minio

# 查看 MinIO 中的物件
docker exec helloworld-minio mc ls local/uploads

# 連線到 MySQL
docker exec -it helloworld-mysql mysql -uhelloworld -phelloworld123 helloworld

# 停止所有服務（保留資料）
docker compose down

# 停止並清除所有資料（慎用）
docker compose down -v
```

---

## 故障排除

### MinIO 連線失敗

確認 MinIO container 健康：

```bash
docker compose ps minio
# STATUS 應顯示 (healthy)
```

確認 `uploads` bucket 存在：

```bash
docker exec helloworld-minio mc ls local/
```

若 bucket 不存在，手動建立：

```bash
docker exec helloworld-minio mc mb local/uploads
```

### Port 衝突

| Port | 服務 |
|------|------|
| 3306 | MySQL |
| 8888 | Spring Boot (本機) |
| 9000 | MinIO S3 API |
| 9001 | MinIO Web Console |

### 切換回本地磁碟儲存

修改 `application.properties`：

```properties
storage.provider=filesystem
```

重啟應用後，檔案將存入 `upload-dir/` 目錄。

---

## 專案結構（儲存相關）

```
service/
├── StorageService.java          # 介面定義
├── MinioStorageService.java      # MinIO 實作（storage.provider=minio）
├── FileSystemStorageService.java # 本地磁碟實作（storage.provider=filesystem）
├── MinioProperties.java          # @ConfigurationProperties("storage.minio")
└── StorageProperties.java        # @ConfigurationProperties("storage")

config/
└── MinioConfig.java              # @Bean MinioClient

domain/model/
├── FileMetadata.java             # 每次上傳的 metadata 記錄
├── StoredFile.java               # 去重後的實體檔案記錄（hash + 物件 key）
└── UploadSession.java            # 分塊上傳 session 狀態
```
