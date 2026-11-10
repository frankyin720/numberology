package com.webest.numerology.util;

import org.springframework.ai.chat.messages.MessageType;

/**
 * API 與資料庫統一使用小寫 {@code user} / {@code assistant}（與 Spring AI {@link MessageType#getValue()} 一致）。
 */
public final class ChatMessageRoles {

    public static final String USER = "user";
    public static final String ASSISTANT = "assistant";

    private ChatMessageRoles() {
    }

    public static String fromMessageType(MessageType type) {
        if (type == MessageType.USER) {
            return USER;
        }
        if (type == MessageType.ASSISTANT) {
            return ASSISTANT;
        }
        throw new IllegalArgumentException("Unsupported message type for persistence: " + type);
    }

    public static boolean isUser(String role) {
        return role != null && USER.equalsIgnoreCase(role.trim());
    }

    public static boolean isAssistant(String role) {
        return role != null && ASSISTANT.equalsIgnoreCase(role.trim());
    }
}
