# SmartATS 技术栈更正说明

**日期**: 2026-02-20
**重要性**: 🔴 高优先级 - 请在开发前阅读

---

## ⚠️ 关键更正：智谱 AI 模型名称

### 之前的文档错误

我之前在文档中使用了过时的模型名称：
- ❌ `glm-4-flash` (已弃用)
- ❌ `glm-4-air` (已弃用)

### 正确的最新模型名称

根据 [智谱 AI 官方文档](https://docs.bigmodel.cn/cn/guide/models/text/glm-4)，GLM-4 系列包含 5 个模型：

| 模型名称 | 类型 | 适用场景 | 价格 |
|---------|------|---------|------|
| **glm-4-plus** | 高智能模型 | 语言理解、逻辑推理、指令遵循 | 按量计费 |
| **glm-4-air-250414** | 基座语言模型 | 工具调用、联网搜索、代码智能体 | 按量计费 |
| **glm-4-airx** | 高速版 | 快速响应 | 按量计费 |
| **glm-4-flashx-250414** | Flash 增强版 | 实时检索、长上下文 | 免费 |
| **glm-4-flash-250414** | 免费语言模型 | 开发测试 | 免费 |

**最新旗舰**：
- **glm-4.7** - Agentic Coding 专用（2026年2月发布）

---

## 📝 推荐配置

### 开发测试环境

```yaml
spring:
  ai:
    zhipuai:
      api-key: ${ZHIPU_API_KEY}
      chat:
        enabled: true
        options:
          # 使用免费的 Flash 模型进行测试
          model: glm-4-flash-250414
          temperature: 0.3
          max-tokens: 4000
```

### 生产环境

```yaml
spring:
  ai:
    zhipuai:
      api-key: ${ZHIPU_API_KEY}
      chat:
        enabled: true
        options:
          # 使用高智能模型
          model: glm-4-plus
          temperature: 0.3
          max-tokens: 4000
```

### 高级场景（Agentic Coding）

```yaml
spring:
  ai:
    zhipuai:
      api-key: ${ZHIPU_API_KEY}
      chat:
        enabled: true
        options:
          # 最新旗舰模型
          model: glm-4.7
          temperature: 0.3
          max-tokens: 65536
```

---

## 🔧 需要更新的文件

请在以下文件中更新模型名称：

1. **.env 文件**
   ```bash
   # 更新为
   ZHIPU_MODEL=glm-4-flash-250414  # 开发测试
   # 或
   ZHIPU_MODEL=glm-4-plus         # 生产环境
   ```

2. **application.yml**
   ```yaml
   spring:
     ai:
       zhipuai:
         chat:
           options:
             model: ${ZHIPU_MODEL:glm-4-flash-250414}
   ```

3. **ZhipuAiConfig.java**
   ```java
   @Value("${spring.ai.zhipuai.chat.options.model:glm-4-flash-250414}")
   private String model;
   ```

---

## 📚 最新官方文档

请参考以下官方文档获取最新信息：

- [GLM-4 模型系列](https://docs.bigmodel.cn/cn/guide/models/text/glm-4)
- [GLM-4.7 最新旗舰](https://docs.bigmodel.cn/cn/guide/start/latest-glm-4.7)
- [智谱 AI 开放平台](https://open.bigmodel.cn/)

---

## ✅ 确认清单

在开始开发前，请确认：

- [ ] 已阅读智谱 AI 官方文档
- [ ] 更新 .env 文件中的模型名称
- [ ] 更新 application.yml 配置
- [ ] 测试 API 调用是否成功

**测试命令**：
```bash
curl -X POST "https://open.bigmodel.cn/api/paas/v4/chat/completions" \
  -H "Authorization: Bearer 你的API密钥" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "glm-4-flash-250414",
    "messages": [{"role": "user", "content": "你好"}]
  }'
```

如果测试成功，说明配置正确，可以开始开发了！

---

**重要提示**：模型名称带有日期后缀（如 `-250414`），这是智谱 AI 的版本管理方式，请确保使用完整名称。
