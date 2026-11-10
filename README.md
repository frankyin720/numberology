# 命理大師 · Angular + Spring Boot + Spring AI

**Angular** 前端 + **Spring Boot 3** + **Spring AI** 命理諮詢應用：繁體中文導師人設、多輪對話與工具調用。

## 功能

- 繁体中文命理导师人设，多轮对话
- **用户注册 / 登录**（Email + 信箱验证后方可登录）
- **多会话**：左侧会话列表，每会话独立上下文（最多 100 条消息）
- **SQLite** 持久化用户、会话与聊天记录（`backend/data/numerology.db`）
- Spring AI `@Tool`：风水、八字、摇卦、解梦、联网（外部 API 不可用时走 LLM）
- **DeepSeek**（`DEEPSEEK_API_KEY`）
- 个人资料：生辰、性别、现居地、时区；聊天内弹窗编辑

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
| `DEEPSEEK_API_KEY` | DeepSeek 聊天 API 密钥 |
| `SPRING_AI_OPENAI_BASE_URL` | 可选；默认 `https://api.deepseek.com`（**勿加** `/v1`） |
| `SPRING_AI_CHAT_MODEL` | 可选；默认 `deepseek-chat` |
| `NUMEROLOGY_DB_PATH` | SQLite 路径，默认 `./data/numerology.db` |
| `YUANFENJU_BASE_URL` | 缘分居 API 根路径 |
| `YUANFENJU_BAZI_PATH` / `ZHANBU` / `ZHOUGONG` | 可选；相对路径或完整 URL |
| `SERPAPI_BASE_URL` | SerpAPI 地址 |
| `YUANFENJU_ENABLED` / `SERPAPI_ENABLED` | 默认 **`false`**（仅用 LLM）；`true` 且配置 Key 后调用外部 API |
| `YUANFENJU_API_KEY` / `SERPAPI_API_KEY` | 可选 |
| `NUMEROLOGY_PUBLIC_URL` | 前端地址（验证信链接），默认 `http://localhost:4200` |
| `SPRING_MAIL_*` / `NUMEROLOGY_MAIL_FROM` | 邮件；未配置时验证链接打印在后端日志 |
| `LLM_LOG_INTERACTIONS` | 是否记录 LLM 往返（默认 `true`） |
| `LOGGING_LEVEL_LLM_EXCHANGE` | 设为 `DEBUG` 可查看完整 LLM 交互 |

### 使用流程

1. 打开前端 → **注册** → 邮箱点击验证链接（开发环境见后端日志 `Verification link`）
2. **登录** → 聊天页；左侧 **＋ 新会话** 创建新对话
3. 点击会话载入历史；请求头 `Authorization: Bearer <token>`
4. **个人资料**（侧栏底部）：补全生辰后方可与大师对话

## 启动

### 后端

- 字符串判空：**Apache Commons Lang3** `StringUtils.isBlank()` / `isNotBlank()`
- POJO：**Lombok**（`@Data`、`@Getter` `@Setter` 等）

```bash
cd backend
mvn spring-boot:run
```

- LLM **REQUEST/RESPONSE** 日志：logger `llm.exchange`，默认 INFO 不显示全文；`logging.level.llm.exchange=DEBUG` 或设置上表环境变量
- 首次启动创建 `backend/data/numerology.db`；JPA `ddl-auto: update` 建表；参考 `backend/src/main/resources/schema/sqlite-schema.sql`

### 前端

```bash
cd frontend
npm start
```

- 打开 http://localhost:4200
- `/api` 代理到 `http://localhost:8080`

---

# Numerology Consultation · Angular + Spring Boot + Spring AI

**Angular** UI + **Spring Boot 3** + **Spring AI**: Traditional Chinese numerology advisor persona, multi-turn chat, and tool calling.

## Features

- Traditional Chinese advisor persona; multi-turn conversation
- **Sign up / log in** (email verification required before login)
- **Multiple chats**: sidebar list; isolated context per session (up to 100 messages)
- **SQLite** persistence for users, sessions, and messages (`backend/data/numerology.db`)
- Spring AI `@Tool`: feng shui, BaZi, divination, dream interpretation, web search (LLM fallback when external APIs are off)
- **DeepSeek** chat API (`DEEPSEEK_API_KEY`)
- Profile: birth data, gender, residence, timezone; editable in an in-chat dialog

## Layout

```
numerology/
├── backend/
├── frontend/
└── README.md
```

## Environment variables

| Variable | Description |
|----------|-------------|
| `DEEPSEEK_API_KEY` | DeepSeek API key |
| `SPRING_AI_OPENAI_BASE_URL` | Optional; default `https://api.deepseek.com` (**do not** append `/v1`) |
| `SPRING_AI_CHAT_MODEL` | Optional; default `deepseek-chat` |
| `NUMEROLOGY_DB_PATH` | SQLite file path; default `./data/numerology.db` |
| `YUANFENJU_BASE_URL` | Yuanfenju API base URL |
| `YUANFENJU_BAZI_PATH` / `ZHANBU` / `ZHOUGONG` | Optional endpoint paths or full URLs |
| `SERPAPI_BASE_URL` | SerpAPI base URL |
| `YUANFENJU_ENABLED` / `SERPAPI_ENABLED` | Default **`false`** (LLM only); set `true` and provide keys to call external APIs |
| `YUANFENJU_API_KEY` / `SERPAPI_API_KEY` | Optional |
| `NUMEROLOGY_PUBLIC_URL` | Public frontend URL for verification emails; default `http://localhost:4200` |
| `SPRING_MAIL_*` / `NUMEROLOGY_MAIL_FROM` | SMTP; verification link is logged if mail is not configured |
| `LLM_LOG_INTERACTIONS` | Enable LLM request/response logging (default `true`) |
| `LOGGING_LEVEL_LLM_EXCHANGE` | Set to `DEBUG` for full LLM exchange logs |

### Getting started

1. Open the app → **Register** → click the verification link in email (dev: see `Verification link` in backend logs)
2. **Log in** → chat page; use **＋ New chat** in the sidebar
3. Select a session to load history; API requests use `Authorization: Bearer <token>`
4. **Profile** (sidebar footer): complete birth profile before full chat access

## Run locally

### Backend

- String checks: **Apache Commons Lang3** `StringUtils.isBlank()` / `isNotBlank()`
- POJOs: **Lombok** (`@Data`, `@Getter` `@Setter`, etc.)

```bash
cd backend
mvn spring-boot:run
```

- LLM **REQUEST/RESPONSE** logs: logger `llm.exchange`; set `logging.level.llm.exchange=DEBUG` for full output (see variables above)
- First run creates `backend/data/numerology.db`; JPA `ddl-auto: update`; see `backend/src/main/resources/schema/sqlite-schema.sql`

### Frontend

```bash
cd frontend
npm start
```

- Open http://localhost:4200
- `/api` is proxied to **http://localhost:8080**
