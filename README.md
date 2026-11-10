# 命理大師 · Angular + Spring Boot + Spring AI

**Angular** 前端 + **Spring Boot 3** + **Spring AI** 命理諮詢應用（繁體中文導師人設、多輪對話與工具調用）。

## 功能

- 繁体中文命理导师人设，多轮对话
- **用户注册 / 登录**（Email + 信箱验证后方可登录）
- **多会话**：左侧会话列表，每个会话独立上下文（最多保留 100 条消息）
- **SQLite** 持久化用户、会话与聊天记录（`backend/data/numerology.db`）
- Spring AI `@Tool`：风水、八字、摇卦、解梦、联网（缘分居不可用时走 LLM）
- **DeepSeek**（`DEEPSEEK_API_KEY`）

## 目录

```
numerology/
├── backend/
├── frontend/
└── README.md
```

## 环境变量

| 变量 | 说明 |
|------|------|
| `DEEPSEEK_API_KEY` | DeepSeek 聊天 |
| `SPRING_AI_OPENAI_BASE_URL` | 可选，默认 `https://api.deepseek.com`（勿加 `/v1`） |
| `SPRING_AI_CHAT_MODEL` | 可选，默认 `deepseek-chat` |
| `NUMEROLOGY_DB_PATH` | 可选，SQLite 文件路径，默认 `./data/numerology.db` |
| `YUANFENJU_BASE_URL` | 缘分居 API 根路径，默认 `https://api.yuanfenju.com/index.php/v1` |
| `YUANFENJU_BAZI_PATH` / `ZHANBU` / `ZHOUGONG` | 可选，相对路径或完整 URL |
| `SERPAPI_BASE_URL` | SerpAPI 搜索地址 |
| `YUANFENJU_ENABLED` / `SERPAPI_ENABLED` | 默认 **`false`**（走 LLM）；设为 `true` 且配置 Key 后才调外部 API |
| `YUANFENJU_API_KEY` / `SERPAPI_API_KEY` | 可选 |
| `NUMEROLOGY_PUBLIC_URL` | 前端地址，用于验证信链接，默认 `http://localhost:4200` |
| `SPRING_MAIL_*` / `NUMEROLOGY_MAIL_FROM` | 可选；未配置时验证链接打印在后端日志 |

### 使用流程

1. 打开前端 → **注册** → 到邮箱点验证链接（开发环境请看后端日志中的 `Verification link`）
2. **登录** → 进入聊天页；左侧 **＋ 新会话** 创建互不相干的对话
3. 点击左侧会话载入该会话全部消息；API 请求头携带 `Authorization: Bearer <token>`

## 启动

### 后端

Java 字符串判空统一使用 **Apache Commons Lang3** 的 `StringUtils.isBlank()` / `isNotBlank()`。  
POJO（DTO、Entity 等）统一使用 **Lombok**（`@Data` / `@Getter` `@Setter` + `@NoArgsConstructor` 等）。

```bash
cd backend
mvn spring-boot:run
```

终端中会打印与 LLM 的 **REQUEST/RESPONSE** 分块日志（logger `llm.exchange`）。关闭：`LLM_LOG_INTERACTIONS=false` 或 `logging.level.llm.exchange=WARN`。

首次启动会自动创建目录 `backend/data/`、文件 `numerology.db`，并由 JPA（`ddl-auto: update`）建表。表结构说明见 `backend/src/main/resources/schema/sqlite-schema.sql`。

### 前端

```bash
cd frontend
npm start
```

http://localhost:4200（`/api` 代理到 8080）
