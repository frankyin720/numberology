package com.webest.numerology.service;

import com.webest.numerology.config.LlmExchangeLogger;
import com.webest.numerology.util.ChatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 外部 API（如缘分居）不可用时，由大模型代为推演回答（无 Tool，避免递归）。
 */
@Component
public class LlmOracleFallback {

    private static final Logger log = LoggerFactory.getLogger(LlmOracleFallback.class);

    private static final String FALLBACK_SYSTEM = """
            你是算命大師陳玉樓，用繁體中文作答。
            當前外部命理 API 不可用，請以大師所學做合理推演；
            開頭一句說明「今日未能接通天機台，以下為大師據古法推演」。
            勿编造可验证的实时新闻；八字若无完整生辰勿捏造四柱，可提示补全信息。
            """;

    private final ChatClient plainClient;
    private final LlmExchangeLogger llmLog;

    public LlmOracleFallback(ChatModel chatModel, LlmExchangeLogger llmLog) {
        this.plainClient = ChatClient.builder(chatModel).build();
        this.llmLog = llmLog;
    }

    public String answer(String taskInstruction, String userContext) {
        String user = taskInstruction + "\n\n用戶內容：\n" + userContext;
        try {
            llmLog.logSimpleRequest("oracle-fallback", FALLBACK_SYSTEM, user);
            var call = plainClient.prompt()
                    .system(FALLBACK_SYSTEM)
                    .user(user)
                    .call();
            var chatResponse = call.chatResponse();
            String content = ChatUtil.getResponseText(chatResponse);
            llmLog.logChatResponse("oracle-fallback", content, chatResponse);
            return content;
        } catch (Exception e) {
            log.warn("LLM fallback failed: {}", e.getMessage());
            return "外部服務與推演均不順，請稍後再問。";
        }
    }
}
