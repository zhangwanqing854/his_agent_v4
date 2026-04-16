# 新电脑环境恢复指南

## 系统要求

| 项目 | 版本要求 |
|-----|---------|
| Node.js | >= 18.x |
| JDK | 17 |
| MySQL | 8.0 |
| Maven | >= 3.8 |

---

## 1. 克隆项目

```bash
git clone https://github.com/zhangwanqing854/his_agent_v4.git
cd his_agent_v4
```

---

## 2. 安装前端依赖

```bash
cd frontend
npm install
```

依赖安装完成后，验证：
```bash
npm run type-check
```

---

## 3. 安装后端依赖

```bash
cd backend
mvn install -DskipTests
```

验证编译：
```bash
mvn compile -DskipTests
```

---

## 4. 数据库恢复

### 4.1 创建数据库用户

```sql
-- 连接 MySQL
mysql -u root -p

-- 创建用户和数据库
CREATE DATABASE IF NOT EXISTS handover_system;
CREATE USER IF NOT EXISTS 'handover'@'localhost' IDENTIFIED BY 'handover123';
GRANT ALL PRIVILEGES ON handover_system.* TO 'handover'@'localhost';
FLUSH PRIVILEGES;
```

### 4.2 恢复数据

```bash
# 从备份文件恢复
mysql -u handover -phandover123 handover_system < database/backup_2026-04-16_handover_system_full.sql
```

### 4.3 验证数据库

```sql
mysql -u handover -phandover123 -e "USE handover_system; SHOW TABLES;"
```

应显示以下核心表：
- `shift_handover`
- `handover_patient`
- `department`
- `his_staff`
- `sms_config`
- `sms_log`
- 等...

---

## 5. 配置文件检查

### 5.1 后端配置

文件：`backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/handover_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: handover
    password: handover123

server:
  port: 8080

xunfei:
  appid: "8bc8b3ca"
  apikey: "ef71686251f3f7b42bbb56c3d737f938"
```

### 5.2 前端配置

确保 `frontend/vite.config.ts` 中后端 API 地址正确：
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

---

## 6. 启动服务

### 6.1 启动后端

```bash
cd backend
mvn spring-boot:run
```

或直接运行：
```bash
java -jar target/handover-system-0.0.1-SNAPSHOT.jar
```

验证后端启动：
```bash
curl http://localhost:8080/api/health
# 或
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d '{"userCode":"admin","password":"admin123"}'
```

### 6.2 启动前端

```bash
cd frontend
npm run dev
```

前端访问地址：http://localhost:3000

---

## 7. HTTPS 配置（语音录入需要）

语音录入功能需要 HTTPS 环境。前端已包含证书文件：
- `frontend/cert.pem`
- `frontend/key.pem`

启动 HTTPS 开发服务器：
```bash
cd frontend
npm run dev -- --https
```

HTTPS 地址：https://localhost:3000

---

## 8. 验证功能

### 8.1 登录测试

浏览器访问 http://localhost:3000

测试账号：
| 用户编码 | 密码 | 角色 |
|---------|------|-----|
| admin | admin123 | 管理员 |
| doctor1 | doctor123 | 医生 |

### 8.2 API 测试

```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userCode":"admin","password":"admin123"}'

# 获取交班列表（需要 token）
curl -X GET "http://localhost:8080/api/handovers?deptId=1" \
  -H "Authorization: Bearer <token>"
```

---

## 9. 常见问题

### Q1: 数据库连接失败

检查 MySQL 服务状态：
```bash
# macOS
brew services list
brew services start mysql

# Linux
sudo systemctl status mysql
sudo systemctl start mysql
```

### Q2: 后端启动失败 - JDK 版本不匹配

检查 JDK 版本：
```bash
java -version  # 应显示 17.x
```

设置 JAVA_HOME：
```bash
# macOS
export JAVA_HOME=/usr/local/opt/openjdk@17

# Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### Q3: 前端 npm install 失败

清除缓存重试：
```bash
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Q4: 类型检查错误

部分预存在类型错误可忽略（不影响运行）：
```bash
cd frontend
npm run type-check 2>&1 | grep -v "request.ts\|scheduling.ts\|user.ts"
```

### Q5: 语音录入不工作

确保使用 HTTPS 访问：
- 浏览器访问 https://localhost:3000
- 需要接受证书警告（开发证书）

---

## 10. 开发工具推荐

| 工具 | 用途 |
|-----|-----|
| VS Code | 前端开发 |
| IntelliJ IDEA | 后端开发 |
| MySQL Workbench | 数据库管理 |
| Postman | API 测试 |

### VS Code 扩展

- Vue - Official (Volar)
- ESLint
- Prettier
- TypeScript Vue Plugin (Volar)

---

## 11. 项目结构概览

```
his_agent_v4/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/          # Java 源码
│   │   └── com/hospital/handover/
│   │       ├── controller/     # REST 控制器
│   │       ├── service/        # 业务服务
│   │       ├── entity/         # 数据实体
│   │       ├── repository/     # 数据访问
│   │       └── dto/            # 数据传输对象
│   └── src/main/resources/
│       └── application.yml     # 配置文件
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 通用组件
│   │   ├── api/                # API 调用
│   │   ├── stores/             # Pinia 状态管理
│   │   └── router/             # 路由配置
│   └ package.json
│   └ vite.config.ts
│
├── database/                   # 数据库脚本
│   ├── backup_2026-04-16_*.sql # 完整备份
│   ├── schema_v2*.sql          # 增量脚本
│   └ BACKUP_README.md          # 恢复说明
│
├── openspec/                   # OpenSpec 变更记录
│   ├── changes/archive/        # 已归档变更
│   └ specs/                    # 功能规范
│
├── docs/                       # 项目文档
├── AGENTS.md                   # AI Agent 配置
└── README.md                   # 项目说明
```

---

## 12. 关键功能模块

| 模块 | 说明 |
|-----|-----|
| 用户认证 | 用户编码登录（不区分大小写） |
| 交班管理 | 创建、提交、接班、退回 |
| 患者管理 | 患者信息聚合与筛选 |
| 短信通知 | 交班提交后自动通知接班医生 |
| 交班编号 | 自动生成唯一编号 (YYMMDDSSS) |
| 语音录入 | 讯飞实时语音转写（需 HTTPS） |
| 数据同步 | HIS/LIS/PACS 数据对接 |

---

## 13. 联系与支持

- GitHub: https://github.com/zhangwanqing854/his_agent_v4
- 备份日期: 2026-04-16
- 数据库备份: `database/backup_2026-04-16_handover_system_full.sql`