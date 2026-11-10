package com.webest.numerology.service;

import com.webest.numerology.util.ChatMessageRoles;
import com.webest.numerology.util.ChatUtil;
import com.webest.numerology.web.dto.ChatRequest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import com.webest.numerology.config.ChatClientConfig;
import com.webest.numerology.config.LlmExchangeLogger;
import com.webest.numerology.repository.entity.ChatConversationEntity;
import com.webest.numerology.store.ChatHistoryStore;
import com.webest.numerology.web.dto.ChatMessageDto;
import com.webest.numerology.web.dto.ChatResultDto;
import com.webest.numerology.web.dto.MessageResponse;
import org.springframework.ai.chat.messages.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NumerologyChatService {

    private static final Logger log = LoggerFactory.getLogger(NumerologyChatService.class);

    private final ChatClient chatClient;
    private final ChatHistoryStore historyStore;
    private final LlmExchangeLogger llmLog;
    private final ConversationService conversationService;

    public NumerologyChatService(
            @Qualifier("numerologyChatClient") ChatClient chatClient,
            ChatHistoryStore historyStore,
            LlmExchangeLogger llmLog,
            ConversationService conversationService) {
        this.chatClient = chatClient;
        this.historyStore = historyStore;
        this.llmLog = llmLog;
        this.conversationService = conversationService;
    }

    @Transactional
    public ChatResultDto chat(Long userId, ChatRequest request) {
        ChatConversationEntity conversation =
                conversationService.requireOwned(userId, request.getConversationId());
        String conversationId = conversation.getId();

        List<Message> messages = new ArrayList<>(historyStore.load(conversationId));
        messages.add(new UserMessage(request.getQuery()));

        log.debug("Chat invoke conversationId={} userQueryChars={}", conversationId, request.getQuery().length());
        llmLog.logChatRequest("numerology-chat", conversationId, ChatClientConfig.SYSTEM_PROMPT, messages);
        ChatResponse chatResponse = chatClient.prompt().messages(messages).call().chatResponse();
        String answer = ChatUtil.getResponseText(chatResponse);
        llmLog.logChatResponse("numerology-chat", answer, chatResponse);
        log.debug("Chat completed conversationId={} answerChars={}", conversationId, answer != null ? answer.length() : 0);
        messages.add(new AssistantMessage(answer));
        historyStore.save(conversationId, messages);
        conversationService.touchAfterMessage(conversation, request.getQuery());

        String result = "【陳大師】\n" + (StringUtils.isBlank(answer) ? "無法獲得答案" : answer);
        log.info("Conversation {} answered", conversationId);
        return new ChatResultDto(conversationId, result);
    }

    public MessageResponse getMessages(String conversationId) {
        List<ChatMessageDto> messages = new ArrayList<>();
        ListUtils.emptyIfNull(historyStore.load(conversationId)).stream().filter(Objects::nonNull)
                .forEach(message -> {
                    if (MessageType.USER.equals(message.getMessageType())) {
                        messages.add(ChatMessageDto.builder()
                                .role(ChatMessageRoles.USER)
                                .content(message.getText())
                                .build());
                    } else if (MessageType.ASSISTANT.equals(message.getMessageType())) {
                        messages.add(ChatMessageDto.builder()
                                .role(ChatMessageRoles.ASSISTANT)
                                .content("【陳大師】\n" + message.getText())
                                .build());
                    }
                });
        return MessageResponse.builder()
                .conversationId(conversationId)
                .messages(messages)
                .build();
    }
}
