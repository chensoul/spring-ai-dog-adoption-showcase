# Spring AI 狗狗领养助手展示项目

一个基于 Spring AI 的智能狗狗领养助手系统，展示了 RAG（检索增强生成）、工具调用、对话记忆等核心 AI 能力。

## 🎯 项目简介

这是一个完整的 Spring AI 应用示例，模拟了一个名为 "Pooch Palace" 的狗狗领养机构。系统包含两个微服务：

- **Assistant（助手服务）**：AI 驱动的对话助手，帮助用户查询和领养狗狗
- **Scheduler（调度服务）**：独立的 MCP 服务器，提供预约调度功能

### 核心能力

- 🤖 **智能问答**：基于 RAG 的语义搜索，准确匹配用户需求
- 💬 **对话记忆**：多用户、多会话的上下文管理
- 🔧 **工具调用**：AI 自动调用远程服务完成预约操作
- 🔍 **向量检索**：使用 pgvector 实现高效的语义搜索

---

## ✨ 功能特性

### 1. RAG（检索增强生成）
- 将狗狗信息向量化存储
- 基于用户问题的语义检索
- 结合检索结果生成准确回复

### 2. 对话记忆
- 按用户隔离的对话历史
- JDBC 持久化存储
- 支持多轮对话上下文

### 3. MCP 工具集成
- 微服务架构，工具远程调用
- 通过 MCP 协议实现服务解耦
- 易于扩展新的工具和服务

### 4. 向量数据库
- 使用 PostgreSQL + pgvector
- 支持高维向量存储和检索
- 自动初始化数据库结构

---

## 🛠 技术栈

### Assistant 应用
- **框架**：Spring Boot 3.5.7
- **AI 框架**：Spring AI 1.1.0
- **数据库**：PostgreSQL 16 + pgvector
- **AI 模型**：通义千问（兼容 OpenAI API）
  - Chat 模型：`qwen-plus`
  - Embedding 模型：`text-embedding-v3`
- **通信协议**：MCP Client（HTTP SSE）

### Scheduler 应用
- **框架**：Spring Boot 3.5.7
- **AI 框架**：Spring AI MCP Server WebMVC
- **端口**：8081

---

## 📦 环境要求

- **JDK**：25（或 Java 17+）
- **Maven**：3.6+
- **PostgreSQL**：16+（带 pgvector 扩展）
- **Docker**（可选）：用于快速启动 PostgreSQL

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/chensoul/spring-ai-dog-adoption-showcase
cd spring-ai-dog-adoption-showcase
```

### 2. 启动 PostgreSQL 数据库

#### 方式一：使用 Docker Compose（推荐）

```bash
cd assistant
docker-compose up -d
```

#### 方式二：手动安装 PostgreSQL

确保 PostgreSQL 已安装并启用 pgvector 扩展：

```sql
CREATE DATABASE mydatabase;
\c mydatabase
CREATE EXTENSION vector;
```

### 3. 配置 API Key

在 `assistant/src/main/resources/application.properties` 中配置通义千问 API Key：

```properties
spring.ai.openai.api-key=${OPENAI_API_KEY:your-dashscope-api-key}
```

或通过环境变量设置：

```bash
export OPENAI_API_KEY=your-dashscope-api-key
```

### 4. 启动 Scheduler 服务

```bash
cd scheduler
./mvnw spring-boot:run
```

服务将在 `http://localhost:8081` 启动。

### 5. 启动 Assistant 服务

```bash
cd assistant
./mvnw spring-boot:run
```

服务将在 `http://localhost:8080` 启动（默认端口）。

### 6. 测试 API

```bash
# 查询可领养的狗狗
curl "http://localhost:8080/alice/ask?question=我想领养一只金毛"

# 预约领养
curl "http://localhost:8080/alice/ask?question=我想预约领养Buddy"
```

---

## 🏗 项目架构

### 项目结构

```
spring-ai-dog-adoption-showcase/
├── assistant/                    # 主应用（AI 助手服务）
│   ├── src/main/java/
│   │   └── com/example/assistant/
│   │       └── AssistantApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── docker-compose.yml        # PostgreSQL 配置
│   └── pom.xml
│
└── scheduler/                    # 调度服务（MCP 服务器）
    ├── src/main/java/
    │   └── com/example/scheduler/
    │       └── SchedulerApplication.java
    ├── src/main/resources/
    │   └── application.properties
    └── pom.xml
```

### 架构图

```
┌─────────────────┐         ┌─────────────────┐
│   User Client   │────────▶│   Assistant     │
│                 │  HTTP   │   (Port 8080)    │
└─────────────────┘         └────────┬────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
            ┌───────▼──────┐  ┌───────▼──────┐  ┌──────▼──────┐
            │   PostgreSQL │  │  通义千问 API │  │  Scheduler  │
            │  + pgvector  │  │              │  │ (Port 8081) │
            └──────────────┘  └──────────────┘  └─────────────┘
```

---

## 📡 API 使用示例

### 1. 查询可领养的狗狗

**请求：**
```bash
GET /alice/ask?question=我想领养一只金毛
```

**响应：**
```
您想领养的金毛是Buddy，它是一只3岁的友好金毛寻回犬，喜欢玩接球游戏，并且非常适合与孩子相处。Buddy目前位于旧金山的Pooch Palace。 如果您有兴趣领养Buddy，我可以帮您安排领养预约。需要我为您继续吗？
```

### 2. 预约领养

**请求：**
```bash
GET /alice/ask?question=我想预约领养Buddy
```

**响应：**
```
已成功为您预约领养Buddy！您的预约时间为2025年11月16日。请在当天前往旧金山的Pooch Palace完成领养手续。 如有其他问题或需要更改预约时间，请随时联系我们。祝您和Buddy早日见面！
```

### 3. 多轮对话

**第一轮：**
```bash
GET /alice/ask?question=有哪些狗狗可以领养？
```

**第二轮（系统会记住上下文）：**
```bash
GET /alice/ask?question=我想了解Luna的详细信息
```

### API 端点说明

- **路径**：`/{user}/ask`
- **方法**：`GET`
- **参数**：
  - `user`（路径参数）：用户ID，用于隔离对话记忆
  - `question`（查询参数）：用户问题
- **返回**：AI 生成的文本回复

---

## ⚙️ 配置说明

### Assistant 应用配置

`assistant/src/main/resources/application.properties`：

```properties
# 应用名称
spring.application.name=assistant

# AI 模型配置（通义千问）
spring.ai.openai.base-url=https://dashscope.aliyuncs.com/compatible-mode
spring.ai.openai.api-key=${OPENAI_API_KEY:your-dashscope-api-key}
spring.ai.openai.chat.options.model=qwen-plus
spring.ai.openai.chat.enabled=true

# Embedding 模型配置
spring.ai.openai.embedding.options.model=text-embedding-v3
spring.ai.model.embedding=openai

# 向量存储配置
spring.ai.vectorstore.pgvector.dimensions=1024
spring.ai.vectorstore.pgvector.initialize-schema=true

# 对话记忆配置
spring.ai.chat.memory.repository.jdbc.initialize-schema=always

# 数据库配置
spring.datasource.url=jdbc:postgresql://localhost/mydatabase
spring.datasource.username=postgres
spring.datasource.password=postgres

# 虚拟线程（Java 21+）
spring.threads.virtual.enabled=true

# Actuator 监控
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

### Scheduler 应用配置

`scheduler/src/main/resources/application.properties`：

```properties
spring.application.name=scheduler
server.port=8081
```

### 使用其他 AI 模型

如果需要使用 OpenAI 官方服务或其他兼容服务，修改配置：

```properties
# OpenAI 官方
spring.ai.openai.base-url=https://api.openai.com/v1
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.embedding.options.model=text-embedding-3-small
spring.ai.vectorstore.pgvector.dimensions=1536

# Azure OpenAI
#spring.ai.openai.base-url=https://your-resource.openai.azure.com
#spring.ai.openai.api-key=${AZURE_OPENAI_API_KEY}
```

---

## 🔧 开发指南

### 添加新的狗狗数据

修改 `AssistantApplication.java` 中的 `initData` 方法：

```java
var dogs = List.of(
    new Dog(6, "NewDog", "Available", "Description..."),
    // 添加更多...
);
```

### 添加新的 MCP 工具

在 `scheduler` 应用中添加新的 `@Tool` 方法：

```java
@Service
class DogAdoptionScheduler {
    
    @Tool(description = "取消预约")
    String cancelSchedule(@ToolParam int dogId) {
        // 实现取消逻辑
        return "已取消预约";
    }
}
```

### 自定义 Advisor

创建自定义 Advisor 来增强 AI 能力：

```java
@Bean
CustomAdvisor customAdvisor() {
    return new CustomAdvisor(/* ... */);
}
```

---

## 📊 数据流详解

### 启动流程

```
1. Assistant 启动
   ├─ 初始化数据库表（dog）
   ├─ 插入示例数据（5条狗狗记录）
   ├─ 将狗狗信息向量化并存入 pgvector
   ├─ 连接 Scheduler MCP 服务器（http://localhost:8081）
   └─ 配置 ChatClient（Advisors + Tools）

2. Scheduler 启动
   ├─ 启动 MCP 服务器（端口 8081）
   └─ 暴露 schedule 工具
```

### 用户请求流程

```
用户请求: GET /alice/ask?question="我想领养一只金毛"

1. AssistantController 接收请求
   ├─ 提取用户ID: "alice"
   └─ 提取问题: "我想领养一只金毛"

2. ChatClient 处理
   ├─ QuestionAnswerAdvisor: 从向量库检索相关狗狗信息
   │  └─ 找到: Buddy (Golden Retriever, San Francisco)
   │
   ├─ PromptChatMemoryAdvisor: 加载用户 "alice" 的对话历史
   │
   └─ AI 生成回复: "我找到一只金毛寻回犬 Buddy，位于旧金山..."

3. 如果用户说"我想预约领养 Buddy"
   ├─ AI 识别需要调用 schedule 工具
   ├─ SyncMcpToolCallbackProvider 转发请求
   ├─ MCP Client → HTTP → Scheduler 服务
   ├─ Scheduler.schedule(1, "Buddy") 执行
   ├─ 返回: "2025-11-16T10:00:00Z" (3天后)
   └─ AI 整合结果: "已为您预约在 2025-11-16 领养 Buddy"
```

---

## 🎨 核心组件说明

### Assistant 应用组件

#### 1. 数据层
```java
record Dog(@Id int id, String name, String owner, String description)
interface DogRepository extends ListCrudRepository<Dog, Integer>
```
- 存储待领养狗狗信息
- 启动时自动初始化 5 条示例数据

#### 2. 向量存储（RAG）
```java
repository.findAll().forEach(dog -> {
    var dogument = new Document("id: %s, name: %s, description: %s"...);
    vectorStore.add(List.of(dogument));
});
```
- 使用 pgvector 存储文档向量
- 维度：1024（通义千问 embedding）
- 支持语义搜索

#### 3. AI 能力配置

- **Chat 模型**：`qwen-plus`
- **Embedding 模型**：`text-embedding-v3`
- **Advisors**：
  - `PromptChatMemoryAdvisor`：对话记忆
  - `QuestionAnswerAdvisor`：向量检索增强
- **Tool Callbacks**：`SyncMcpToolCallbackProvider`

### Scheduler 应用组件

#### MCP 工具定义
```java
@Tool(description = "schedule an appointment to pick up or adopt a dog...")
String schedule(@ToolParam int dogId, @ToolParam String dogName)
```
- 功能：预约领养/接狗
- 逻辑：当前时间 + 3 天
- 返回：ISO 格式时间字符串

---

## ❓ 常见问题

### Q1: 如何更换 AI 模型？

修改 `application.properties` 中的模型配置，并确保 API Key 正确。

### Q2: 向量维度不匹配怎么办？

不同模型的 embedding 维度不同：
- 通义千问 `text-embedding-v3`：1024
- OpenAI `text-embedding-3-small`：1536
- OpenAI `text-embedding-3-large`：3072

修改 `spring.ai.vectorstore.pgvector.dimensions` 配置。

### Q3: Scheduler 服务无法连接？

确保：
1. Scheduler 服务已启动（端口 8081）
2. 网络连接正常
3. MCP 客户端配置正确

### Q4: 数据库连接失败？

检查：
1. PostgreSQL 是否运行
2. 数据库名称、用户名、密码是否正确
3. pgvector 扩展是否已安装

### Q5: API Key 如何获取？

- **通义千问**：访问 [阿里云百炼控制台](https://dashscope.console.aliyun.com/)
- **OpenAI**：访问 [OpenAI Platform](https://platform.openai.com/)

---

## 🎯 设计亮点

- ✅ **微服务架构**：Assistant 和 Scheduler 解耦，易于扩展
- ✅ **RAG 增强**：向量检索提升回答准确性
- ✅ **工具集成**：AI 可自动调用外部服务
- ✅ **对话记忆**：多用户、多会话支持
- ✅ **可扩展性**：通过 MCP 协议轻松添加新工具
- ✅ **生产就绪**：包含监控、健康检查等特性

---

## 📝 许可证

查看 [LICENSE](LICENSE) 文件了解详情。

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📚 相关资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [MCP 协议规范](https://modelcontextprotocol.io/)
- [pgvector 文档](https://github.com/pgvector/pgvector)
- [通义千问文档](https://help.aliyun.com/zh/model-studio/)

---

**这是一个展示 Spring AI 能力的完整示例，涵盖了 RAG、工具调用、对话记忆等核心功能。**
