# MySQL Docker Compose 设置指南

## 快速启动

### 1. 启动 MySQL 数据库
```bash
docker-compose up -d
```

### 2. 检查容器状态
```bash
docker-compose ps
```
确保容器状态为 `healthy`。

### 3. 启动 Spring Boot 应用
```bash
./mvnw spring-boot:run
```

## 数据库连接信息

- **主机**: localhost
- **端口**: 3306
- **数据库名**: helloworld
- **用户名**: helloworld
- **密码**: helloworld123
- **Root 密码**: rootpassword

## 常用命令

### 查看 MySQL 日志
```bash
docker-compose logs -f mysql
```

### 连接到 MySQL 命令行
```bash
docker exec -it helloworld-mysql mysql -uhelloworld -phelloworld123
```

### 停止数据库
```bash
docker-compose down
```

### 停止并删除数据（谨慎使用）
```bash
docker-compose down -v
```

## 测试 API

应用启动后，可以测试以下 endpoint：

```bash
curl http://localhost:8081/employee
```

## 配置文件

- `docker-compose.yml` - Docker Compose 配置
- `src/main/resources/application.properties` - Spring Boot 数据库配置
- `pom.xml` - Maven 依赖（已添加 spring-boot-starter-data-jpa）

## 注意事项

1. 应用启动时会自动连接到 MySQL 数据库
2. 使用 JPA Hibernate `ddl-auto=update` 自动创建/更新表结构
3. 数据持久化在 Docker volume `mysql_data` 中
4. 现有的 Repository 类仍使用内存存储，将来可以迁移到使用 JPA

## 故障排除

### 端口冲突
如果 3306 端口已被占用，修改 `docker-compose.yml` 中的端口映射：
```yaml
ports:
  - "3307:3306"  # 改为本地 3307 端口
```
同时更新 `application.properties` 中的连接 URL：
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/helloworld...
```

### 连接被拒绝
确保 MySQL 容器完全启动（状态为 healthy）：
```bash
docker-compose ps
```
