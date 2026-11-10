-- 使用者與驗證（JPA ddl-auto=update 會自動建表）
-- users, auth_tokens, chat_conversations, chat_messages

CREATE TABLE IF NOT EXISTS chat_messages (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id  VARCHAR(64)  NOT NULL,
    role        VARCHAR(16)  NOT NULL,
    content     TEXT         NOT NULL,
    sort_order  INTEGER      NOT NULL,
    created_at  TEXT         NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session ON chat_messages (session_id, sort_order);
