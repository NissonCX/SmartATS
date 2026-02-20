# SmartATS — 智能招聘管理系统

> 面向 HR 的 AI 驱动简历解析与人才搜索平台

---

## 项目简介

**SmartATS**（Smart Applicant Tracking System）是一套面向 HR 的智能招聘管理系统，核心链路分为四条：

| 链路 | 功能描述 | 状态 |
|------|---------|------|
| **登录链路** | 注册 / 登录 / JWT 鉴权，三种角色（ADMIN / HR / INTERVIEWER） | ✅ 95% |
| **上传链路** | 批量上传简历 → MD5 去重 → MinIO 存储 → MQ 异步解析 → AI 结构化提取 | ✅ 85% |
| **检索链路** | 关键词筛选 + RAG 语义搜索（向量相似度 + LLM 重排）候选人 | ⏳ 60% |
| **招聘流程链路** | 职位管理 → 简历投递 → 面试安排 → 面试反馈 | ⏳ 50% |

**整体完成度**: ~80%

---

## 技术栈

| 层次 | 技术 | 版本 | 用途 | 状态 |
|------|------|------|------|------|
| 核心框架 | Spring Boot | 3.2.5 | 基础框架 | ✅ |
| 运行时 | JDK | 21 | Java 运行环境 | ✅ 必需 |
| ORM | MyBatis-Plus | 3.5.10.1 | 数据库操作（LambdaQueryWrapper） | ✅ |
| 数据库 | MySQL | 8.0 | 业务数据持久化，含全文索引 | ✅ |
| 缓存 / 锁 | Redis | 7.0 | 缓存、分布式去重、原子计数 | ✅ |
| 分布式锁 | Redisson | 3.25.0 | Watchdog 自动续期分布式锁 | ✅ |
| 消息队列 | RabbitMQ | 3.12 | 简历解析任务异步解耦、死信补偿 | ✅ |
| 对象存储 | MinIO | 8.5.10 | 简历文件存储 | ✅ |
| AI 集成 | Spring AI | 1.0.0-M4 | 智谱AI 调用 + Embedding | ✅ |
| 文档处理 | Apache POI / PDFBox | 5.2.5 / 2.0.29 | DOC/DOCX/PDF 内容提取 | ✅ |
| 认证 | Spring Security + JWT | - | 接口鉴权、BCrypt 密码加密 | ✅ |
| 邮件 | Spring Mail | - | HTML 验证码邮件（QQ SMTP） | ✅ |
| JSON 工具 | Fastjson2 | 2.0.43 | 序列化 | ✅ |
| 工具库 | Hutool | 5.8.23 | 加密、时间、字符串 | ✅ |

---

## 系统架构

```
┌──────────┐     ┌────────────────────────────────────────────────────────────┐
│ HR Client│────▶│  API 层：Spring Security JWT 过滤器                         │
└──────────┘     └──────────────────────────┬───────────────────────────────┘
                                            │
        ┌───────────────────────────────────┼──────────────────────────────┐
        ▼                                   ▼                              ▼
┌──────────────┐                   ┌──────────────────┐           ┌──────────────┐
│   认证模块    │                   │    简历模块        │           │   职位模块   │
│  注册 登录    │                   │  批量上传/去重     │           │  CRUD + 缓存 │
│  JWT 工具    │                   │  MD5 → MinIO      │           │  热榜 ZSet   │
│  JWT 过滤器  │                   │  状态轮询          │           └──────────────┘
└──────────────┘                   └────────┬──────────┘
                                            │ 发 MQ 消息
                                            ▼
                                   ┌──────────────────┐
                                   │    RabbitMQ       │
                                   │  resume.parse     │
                                   │  .queue           │
                                   │  DLX → DLQ 死信   │
                                   └────────┬──────────┘
                                            │ 消费
                                            ▼
                                   ┌────────────────────────┐
                                   │  解析消费者              │
                                   │  1. 幂等检查            │
                                   │  2. Redisson 分布式锁   │
                                   │  3. 文件内容提取        │
                                   │  4. AI 结构化提取       │
                                   │  5. 写 MySQL candidates │
                                   │  6. Webhook 通知        │
                                   └────────────────────────┘
```

---

## 快速启动

### 前置依赖

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 21 | 必须，低版本不兼容 |
| Maven | 3.9+ | 构建工具 |
| Docker Desktop | 最新版 | 运行所有基础设施服务 |

### 第一步：启动基础设施

```bash
git clone <repo-url>
cd SmartATS

# 一键启动 MySQL / Redis / RabbitMQ / MinIO
docker-compose up -d

# 验证服务健康
docker-compose ps
```

| 服务 | 地址 | 账号 / 密码 |
|------|------|------------|
| MySQL | `localhost:3307` | `smartats` / `smartats123` |
| Redis | `localhost:6379` | 密码：`redis123` |
| RabbitMQ 管理界面 | `http://localhost:15672` | `admin` / `admin123`，VHost：`smartats` |
| MinIO 控制台 | `http://localhost:9001` | `admin` / `admin123456` |

### 第二步：初始化数据库

```bash
# 初始化数据库表
mysql -h 127.0.0.1 -P 3307 -u smartats -psmartats123 smartats < docker/mysql/init/01-init-database.sql

# 创建 webhook 表
mysql -h 127.0.0.1 -P 3307 -u smartats -psmartats123 smartats < src/main/resources/db/webhook_tables.sql
```

### 第三步：配置环境变量

创建 `.env` 文件（已加入 `.gitignore`）：

```env
# 智谱AI（必需）
ZHIPU_API_KEY=your_api_key_here
ZHIPU_MODEL=glm-4-flash-250414

# 邮件功能（可选）
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_qq_smtp_auth_code

# JWT密钥（生产环境必须修改）
JWT_SECRET=your_production_secret_key_minimum_32_characters
```

### 第四步：构建并运行

```bash
mvn clean install
mvn spring-boot:run
```

应用启动后监听：`http://localhost:8080/api/v1`

---

## 数据库设计

共 8 张核心表：

### 表关系概览

```
users ──┐
        ├──▶ jobs ──────────────────────────────────┐
        │                                           │
        └──▶ resumes ──▶ candidates ────────────────┤
                                  └──▶ job_applications ──▶ interview_records
```

### 各表说明

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `users` | 账号体系 | `role`（ADMIN/HR/INTERVIEWER），`daily_ai_quota` 每日 AI 配额 |
| `jobs` | 职位信息 | `status`（DRAFT/PUBLISHED/CLOSED），`required_skills` JSON |
| `resumes` | 简历文件 | `file_hash` MD5 唯一索引，`status` 状态流转 |
| `candidates` | AI 提取结构化数据 | `skills` JSON，`work_experiences` JSON，`raw_json` |
| `job_applications` | 投递记录 | `match_score` AI 匹配分 |
| `interview_records` | 面试记录 | `round` 轮次，`recommendation` 推荐级别 |
| `webhook_configs` | Webhook 配置 | `event_types` JSON，`secret` 签名密钥 |
| `webhook_logs` | Webhook 日志 | `status`，`response_body` |

---

## API 接口文档

**Base URL：** `/api/v1`

**认证方式：** 除注册/登录外，所有接口需在请求头携带：
```
Authorization: Bearer <accessToken>
```

**统一响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}
```

### 认证模块 `/auth`

| 方法 | 路径 | 说明 | 需要 Token |
|------|------|------|:---------:|
| POST | `/auth/register` | 用户注册（BCrypt 密码加密） | ❌ |
| POST | `/auth/login` | 登录，返回 accessToken（2h）+ refreshToken（7d） | ❌ |
| POST | `/auth/send-verification-code` | 发送邮箱验证码（HTML 模板，5 分钟有效） | ❌ |
| GET | `/auth/test` | 测试认证状态 | ✅ |

### 职位模块 `/jobs`

| 方法 | 路径 | 说明 | 需要 Token |
|------|------|------|:---------:|
| POST | `/jobs` | 创建职位 | ✅ |
| PUT | `/jobs` | 更新职位 | ✅ |
| GET | `/jobs/{id}` | 获取职位详情（Redis 缓存） | ❌ |
| GET | `/jobs` | 分页查询职位列表 | ❌ |
| POST | `/jobs/{id}/publish` | 发布职位 | ✅ |
| POST | `/jobs/{id}/close` | 关闭职位 | ✅ |
| DELETE | `/jobs/{id}` | 删除职位 | ✅ |
| GET | `/jobs/hot` | 获取热门职位排行（Redis ZSet） | ❌ |

### 简历模块 `/resumes`

| 方法 | 路径 | 说明 | 需要 Token |
|------|------|------|:---------:|
| POST | `/resumes/upload` | 单文件上传（PDF/DOC/DOCX，≤ 10MB） | ✅ |
| GET | `/resumes/tasks/{taskId}` | 查询解析任务状态 | ✅ |
| GET | `/resumes/{id}` | 获取简历详情 | ✅ |
| GET | `/resumes` | 分页查询简历列表 | ✅ |

### 候选人模块 `/candidates`

| 方法 | 路径 | 说明 | 需要 Token |
|------|------|------|:---------:|
| GET | `/candidates/{id}` | 获取候选人详情 | ✅ |
| GET | `/candidates` | 分页查询候选人（关键字搜索） | ✅ |
| PUT | `/candidates/{id}` | 更新候选人信息 | ✅ |
| DELETE | `/candidates/{id}` | 删除候选人 | ✅ |

### Webhook 模块 `/webhooks`

| 方法 | 路径 | 说明 | 需要 Token |
|------|------|------|:---------:|
| POST | `/webhooks` | 创建 Webhook 配置 | ✅ |
| GET | `/webhooks` | 查询 Webhook 列表 | ✅ |
| DELETE | `/webhooks/{id}` | 删除 Webhook 配置 | ✅ |

---

## Redis 规范

### Key 命名一览

| Key 模式 | 类型 | 用途 | TTL |
|----------|------|------|-----|
| `jwt:token:{userId}` | String | Access Token（验证） | 2h |
| `jwt:refresh:{userId}` | String | Refresh Token | 7d |
| `verification_code:{email}` | String | 邮箱验证码 | 5min |
| `verification_code-limit:{email}` | String | 验证码发送频率限制 | 60s |
| `task:resume:{taskId}` | Hash | 解析任务状态 | 24h |
| `idempotent:resume:{resumeId}` | String | 幂等性检查 | 1h |
| `dedup:resume:{fileHash}` | String | 文件 MD5 去重标记 | 7d |
| `lock:resume:{fileHash}` | String | 解析分布式锁 | 自动释放 |
| `cache:job:{jobId}` | String | 职位详情缓存 | 30min |
| `cache:job:hot` | ZSet | 热门职位排行 | 10min |
| `counter:job:view:{jobId}` | String | 原子浏览计数器 | 持久化 |

---

## RabbitMQ 拓扑

```
Producer
  └──▶ smartats.exchange（Direct Exchange，durable）
         │
         │  routing_key: resume.parse
         ▼
       resume.parse.queue（主队列）
         │ x-dead-letter-exchange: smartats.dlx
         │
         │  Consumer 手动 ACK
         │    1. 幂等检查（Redis 标记）
         │    2. Redisson 锁（fileHash 维度）
         │    3. 更新 Redis 状态 PROCESSING
         │    4. 文件内容提取（POI/PDFBox）
         │    5. AI 解析（智谱AI）
         │    6. 写 MySQL（candidates）
         │    7. Webhook 通知
         │    8. 更新状态 COMPLETED → ACK
         │
         │  处理异常 → NACK → 死信
         ▼
       smartats.dlx → resume.parse.dlq（死信队列）
```

---

## AI 集成

### 智谱AI配置

项目使用 Spring AI 集成智谱AI，完全兼容 OpenAI 格式：

```yaml
spring:
  ai:
    openai:
      api-key: ${ZHIPU_API_KEY}
      base-url: https://open.bigmodel.cn/api/paas/v4
      chat:
        enabled: true
        options:
          model: glm-4-flash-250414  # 免费模型
          temperature: 0.3
          max-tokens: 4000
```

### 简历解析流程

1. 文件上传到 MinIO
2. 消费者从 MinIO 下载文件
3. 使用 POI/PDFBox 提取文本内容
4. 调用智谱AI解析，输出结构化JSON
5. 保存到 candidates 表

---

## 开发规范

### 数据库操作

```java
// ✅ 正确：LambdaQueryWrapper（类型安全，重构友好）
userMapper.selectOne(new LambdaQueryWrapper<User>()
    .eq(User::getUsername, username));

// ❌ 错误：字符串形式（运行时才报错，重构不友好）
userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
```

### 核心规范

- 业务异常统一抛出 `BusinessException(ResultCode.xxx)`
- 密码必须 BCrypt 加密，响应中**禁止**返回密码字段
- 多步数据库操作使用 `@Transactional(rollbackFor = Exception.class)`
- **写操作后删除缓存**（而非更新），防止脏读

### 日志规范

| 级别 | 使用场景 |
|------|---------|
| `INFO` | 业务里程碑（上传成功、解析完成） |
| `WARN` | 潜在问题（缓存频繁未命中、重试次数增加） |
| `ERROR` | 系统级错误（AI 调用失败、MQ 消息处理异常） |
| `DEBUG` | 详细调试信息（生产环境关闭） |

---

## 目录结构

```
src/main/java/com/smartats/
├── SmartAtsApplication.java                # 启动类
├── common/                                 # 公共组件
│   ├── constants/                          # 常量定义
│   ├── exception/                          # BusinessException、GlobalExceptionHandler
│   ├── result/                             # Result<T> 统一响应、ResultCode 错误码
│   └── util/                               # FileValidationUtil 等工具类
├── config/                                 # 配置类
│   ├── SecurityConfig.java                 # Spring Security + CORS
│   ├── RabbitMQConfig.java                 # Exchange、Queue、DLQ
│   ├── MinioConfig.java                    # MinIO 客户端
│   ├── RedissonConfig.java                 # Redisson 分布式锁
│   └── ZhipuAiConfig.java                  # 智谱 AI 配置
├── infrastructure/                         # 基础设施服务
│   ├── email/                              # EmailService
│   ├── mq/                                 # MessagePublisher
│   └── storage/                            # MinioFileStorageService
└── module/                                 # 业务模块
    ├── auth/                               # ✅ 认证模块 (95%)
    ├── job/                                # ✅ 职位管理 (90%)
    ├── resume/                             # ✅ 简历上传 (85%)
    │   └── consumer/                       # ResumeParseConsumer 消费者
    ├── candidate/                          # ⏳ 候选人模块 (60%)
    ├── webhook/                            # ⏳ Webhook (70%)
    └── interview/                          # ❌ 面试模块 (0%)
```

---

## 项目进度

### 当前状态（2026-02-20）

| 模块 | 完成度 | 状态 |
|------|--------|------|
| 项目骨架（统一响应 / 全局异常） | 100% | ✅ 完成 |
| Spring Security 配置 | 100% | ✅ 完成 |
| 认证模块（注册 / 登录 / JWT） | 95% | ✅ 完成 |
| 职位管理模块（CRUD / 缓存 / 热榜） | 90% | ✅ 完成 |
| 简历上传模块（上传 / 去重 / MQ） | 85% | ✅ 完成 |
| AI 解析（智谱AI集成） | 80% | ✅ 完成 |
| Webhook 模块 | 70% | ⏳ 部分完成 |
| 候选人模块 | 60% | ⏳ 部分完成 |
| 面试模块 | 0% | ❌ 未开始 |
| 单元测试覆盖 | 5% | ⚠️ 严重不足 |

### 下一步计划

| 优先级 | 任务 | 预计时间 |
|--------|------|----------|
| 🔴 高 | 补充单元测试 | 1-2周 |
| 🟡 中 | 完善候选人高级搜索 | 3-5天 |
| 🟡 中 | 完善Webhook测试接口 | 1天 |
| 🟢 低 | 实现面试模块 | 2周 |
| 🟢 低 | 向量数据库集成 | 2-3周 |

---

## 已知问题

| 优先级 | 问题 | 说明 |
|--------|------|------|
| 🔴 高 | 测试覆盖率为0 | 核心业务逻辑无测试 |
| 🟡 中 | Webhook测试接口未实现 | `WebhookController.java:86` |
| 🟡 中 | 批量上传功能缺失 | 仅有单文件上传 |
| 🟢 低 | 面试模块未开发 | 数据库表已就绪 |

---

## 参考文档

| 文档 | 说明 |
|------|------|
| [项目进度总结](docs/project-progress-summary.md) | 详细的模块完成情况分析 |
| [SmartATS 设计文档](docs/SmartATS-Design-Document.md) | 完整技术规范：数据库 Schema、全量 API 定义 |
| [从0到1开发教学手册](docs/SmartATS-从0到1开发教学手册.md) | 分阶段开发指南、新手踩坑清单 |
| [智谱AI模型参考](docs/zhipu-models-quick-reference.md) | 智谱AI模型快速配置 |

---

## 开发环境

**IDE**: IntelliJ IDEA（推荐）
**JDK**: 21
**构建工具**: Maven 3.9+
**数据库**: MySQL 8.0 + Redis 7.0
**消息队列**: RabbitMQ 3.12
**对象存储**: MinIO

---

## License

MIT License

---

**最后更新**: 2026年2月20日
**版本**: 1.0.0
**项目状态**: 开发中（80%完成）
