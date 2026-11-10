package com.webest.numerology.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 格式化打印与 LLM 的请求/响应，便于在控制台查看。
 * Logger 名称：{@code llm.exchange}，日志级别为 DEBUG；应用默认 INFO 下不输出，
 * 调试时设置 {@code logging.level.llm.exchange=DEBUG}。
 */
@Component
public class LlmExchangeLogger {

    private static final Logger log = LoggerFactory.getLogger("llm.exchange");

    @Value("${numerology.llm.log-interactions:true}")
    private boolean enabled;

    public void logChatRequest(String scene, String sessionId, String defaultSystem, List<Message> messages) {
        if (!enabled || !log.isDebugEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(line('=')).append("\n");
        sb.append("LLM REQUEST  [").append(scene).append("]");
        if (StringUtils.isNotBlank(sessionId)) {
            sb.append("  session=").append(sessionId);
        }
        sb.append("\n");
        if (StringUtils.isNotBlank(defaultSystem)) {
            appendBlock(sb, "system (default)", defaultSystem);
        }
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            appendBlock(sb, roleLabel(m) + " #" + (i + 1), textOf(m));
        }
        sb.append(line('='));
        log.debug("\n{}", sb);
    }

    public void logSimpleRequest(String scene, String system, String user) {
        if (!enabled || !log.isDebugEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(line('=')).append("\n");
        sb.append("LLM REQUEST  [").append(scene).append("]\n");
        if (StringUtils.isNotBlank(system)) {
            appendBlock(sb, "system", system);
        }
        if (StringUtils.isNotBlank(user)) {
            appendBlock(sb, "user", user);
        }
        sb.append(line('='));
        log.debug("\n{}", sb);
    }

    public void logChatResponse(String scene, String content, ChatResponse chatResponse) {
        if (!enabled || !log.isDebugEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(line('-')).append("\n");
        sb.append("LLM RESPONSE [").append(scene).append("]\n");
        appendBlock(sb, "assistant", content != null ? content : "(null)");
        if (chatResponse != null && chatResponse.getMetadata() != null
                && !chatResponse.getMetadata().isEmpty()) {
            appendBlock(sb, "metadata", chatResponse.getMetadata().toString());
        }
        sb.append(line('-'));
        log.debug("\n{}", sb);
    }

    public void logTextResponse(String scene, String content) {
        logChatResponse(scene, content, null);
    }

    private static void appendBlock(StringBuilder sb, String title, String body) {
        sb.append("--- ").append(title).append(" ---\n");
        sb.append(body != null ? body.stripTrailing() : "").append("\n\n");
    }

    private static String roleLabel(Message m) {
        MessageType type = m.getMessageType();
        if (type == MessageType.USER) {
            return "user";
        }
        if (type == MessageType.ASSISTANT) {
            return "assistant";
        }
        if (type == MessageType.SYSTEM) {
            return "system";
        }
        return type != null ? type.name().toLowerCase() : "message";
    }

    private static String textOf(Message m) {
        try {
            return m.getText();
        } catch (Exception e) {
            return String.valueOf(m);
        }
    }

    private static String line(char c) {
        return String.valueOf(c).repeat(72);
    }
}
