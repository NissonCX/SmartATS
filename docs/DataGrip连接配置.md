# DataGrip 数据库连接配置指南

## 连接信息

| 属性 | 值 |
|------|-----|
| **数据库类型** | MySQL |
| **主机** | localhost |
| **端口** | 3307 ⚠️ 注意不是 3306 |
| **数据库** | smartats |
| **用户名** | smartats |
| **密码** | smartats123 |
| **SSH 隧道** | 不需要 |

## 详细步骤

### 1. 创建新连接

1. 点击左上角 **+** 号
2. 选择 **Data Source** → **MySQL**

### 2. 填写连接信息

```
Host: localhost
Port: 3307
User: smartats
Password: smartats123
Database: smartats
```

### 3. 测试连接

- 点击 **Test Connection** 按钮
- 首次会提示下载驱动，点击 **Download** 下载 MySQL 驱动
- 看到 "Succeeded" 表示连接成功

### 4. 查看数据

连接成功后，你可以在左侧看到：
- `users` - 用户表（含测试账号）
- `jobs` - 职位表
- `resumes` - 简历文件表
- `candidates` - 候选人表
- `job_applications` - 申请表
- `interview_records` - 面试记录表

右键点击表名 → **Open Table** 可以查看数据

---

## IDEA Database Tool 连接配置

### 步骤 1：打开数据库工具窗口

- 菜单栏：**View** → **Tool Windows** → **Database**
- 或快捷键：**Cmd + ;** (Mac) 或 **Ctrl + ;** (Windows)

### 步骤 2：添加数据源

1. 点击 **Database** 面板左上角 **+** 号
2. 选择 **Data Source** → **MySQL**

### 步骤 3：配置连接（与 DataGrip 相同）

```
Host: localhost
Port: 3307
User: smartats
Password: smartats123
Database: smartats
```

### 步骤 4：测试连接并下载驱动

点击 **Test Connection**，首次会提示下载驱动。

---

## 常见问题

### Q1: 连接失败，提示 "Communications link failure"

**原因**：Docker 容器没有启动

**解决**：
```bash
docker-compose ps
# 如果没有运行，执行：
docker-compose up -d
```

### Q2: 连接失败，提示 "Access denied for user"

**原因**：用户名或密码错误

**确认**：
- 用户名：smartats（不是 root）
- 密码：smartats123（不是 root123）

### Q3: 端口连接错误

**原因**：使用了默认的 3306 端口

**解决**：改为 **3307**

---

## SQL 查询示例

连接成功后，可以尝试这些查询：

```sql
-- 查看所有用户
SELECT * FROM users;

-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESCRIBE users;

-- 统计每个状态的简历数量
SELECT status, COUNT(*) as count
FROM resumes
GROUP BY status;

-- 查看最近上传的简历
SELECT * FROM resumes
ORDER BY created_at DESC
LIMIT 10;
```
