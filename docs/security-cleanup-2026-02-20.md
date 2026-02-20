# SmartATS 安全修复报告

**日期**: 2026-02-20
**事件**: GitGuardian 检测到敏感信息泄露

---

## 🔴 检测到的问题

### 1. MySQL 证书私钥泄露
- **文件**: `data/mysql/*.pem`, `data/mysql/*.key`
- **类型**: Generic Private Key
- **风险**: 数据库 SSL 连接私钥泄露

### 2. SMTP 邮件凭证泄露
- **文件**: `src/main/resources/application.yml`
- **类型**: SMTP credentials
- **风险**: 邮件发送凭证泄露

---

## ✅ 已执行的修复措施

### 1. 使用 git-filter-repo 清理 git 历史

```bash
# 创建备份分支
git branch backup-before-cleanup-20260220

# 使用 git-filter-repo 移除 data/mysql/ 目录
git filter-repo --path data/mysql/ --invert-paths --force
```

### 2. 更新 application.yml 使用环境变量

| 配置项 | 修复前 | 修复后 |
|--------|--------|--------|
| 邮件用户名 | `2144546224@qq.com` | `${MAIL_USERNAME:your_email@qq.com}` |
| 邮件密码 | `smartats123` | `${MAIL_PASSWORD}` |
| 数据库密码 | `smartats123` | `${DB_PASSWORD}` |
| Redis 密码 | `redis123` | `${REDIS_PASSWORD}` |
| RabbitMQ 密码 | `admin123` | `${RABBITMQ_PASSWORD}` |
| JWT 密钥 | `SmartATS2026...` | `${JWT_SECRET:...}` |
| MinIO 密钥 | `admin123456` | `${MINIO_SECRET_KEY:...}` |

### 3. 创建 application.yml.example 模板

提供配置模板，所有敏感值均使用环境变量占位符：

```yaml
# 邮件配置
spring:
  mail:
    username: ${MAIL_USERNAME:your_email@qq.com}
    password: ${MAIL_PASSWORD}

# 数据库配置
  datasource:
    password: ${DB_PASSWORD}

# Redis 配置
  data:
    redis:
      password: ${REDIS_PASSWORD}

# RabbitMQ 配置
  rabbitmq:
    password: ${RABBITMQ_PASSWORD}

# JWT 配置
smartats:
  jwt:
    secret: ${JWT_SECRET:...}

# MinIO 配置
minio:
  access-key: ${MINIO_ACCESS_KEY:admin}
  secret-key: ${MINIO_SECRET_KEY:admin123456}
```

### 4. 更新 .gitignore

已添加以下规则以防止未来提交敏感文件：

```
# 证书和密钥文件
*.pem
*.key
*.crt
*.p12
*.jks

# 运行时数据
data/

# 敏感配置
application-local.yml
application-dev.yml
application-prod.yml
.env
.env.local
```

---

## 📊 验证结果

| 检查项 | 结果 |
|--------|------|
| data/mysql/ 历史文件清理 | ✅ 0 个文件残留 |
| application.yml 环境变量化 | ✅ 全部更新 |
| .gitignore 规则完善 | ✅ 已添加 |
| 远程仓库强制推送 | ✅ 成功 |
| 备份分支创建 | ✅ backup-before-cleanup-20260220 |

---

## 🔧 后续操作指南

### 开发环境配置

创建 `.env` 文件（本地开发，不提交到 git）：

```bash
# .env 文件示例
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_mail_password

DB_PASSWORD=your_db_password
REDIS_PASSWORD=your_redis_password
RABBITMQ_PASSWORD=your_rabbitmq_password

JWT_SECRET=your_jwt_secret_key_min_256_bits
MINIO_SECRET_KEY=your_minio_secret
```

### 生产环境配置

使用环境变量或配置中心（如 Nacos、Apollo）：

```bash
export MAIL_PASSWORD=xxx
export DB_PASSWORD=xxx
export REDIS_PASSWORD=xxx
export RABBITMQ_PASSWORD=xxx
export JWT_SECRET=xxx
export MINIO_SECRET_KEY=xxx
```

---

## 📝 提交记录

```
0e64461 security: 移除敏感信息并添加配置模板
38f06bf chore: 添加 .gitignore 并清理不必要的文件
```

---

## ⚠️ 重要提醒

1. **轮换已泄露的凭证**：建议更换所有已泄露的密码和密钥
2. **检查克隆仓库**：如果有其他人克隆了此仓库，需要通知他们使用最新版本
3. **定期扫描**：建议配置 GitGuardian 或类似工具定期扫描代码仓库

---

## 🔗 参考文档

- [git-filter-repo 文档](https://github.com/newren/git-filter-repo)
- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [GitGuardian 最佳实践](https://docs.gitguardian.com/secrets-detection)
