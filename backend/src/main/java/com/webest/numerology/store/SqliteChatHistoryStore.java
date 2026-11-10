package com.webest.numerology.store;

import com.webest.numerology.repository.ChatMessageRepository;
import com.webest.numerology.repository.entity.ChatMessageEntity;
import com.webest.numerology.util.ChatMessageRoles;
import org.apache.commons.collections4.ListUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SqliteChatHistoryStore implements ChatHistoryStore {

    private static final int MAX_MESSAGES = 100;

    private final ChatMessageRepository repository;

    public SqliteChatHistoryStore(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> load(String sessionId) {
        List<Message> messages = new ArrayList<>();
        ListUtils.emptyIfNull(this.repository.findBySessionIdOrderBySortOrderAsc(sessionId)).stream().filter(Objects::nonNull)
                .forEach(message -> {
                    String role = message.getRole();
                    if (ChatMessageRoles.isUser(role)) {
                        messages.add(new UserMessage(message.getContent()));
                    } else if (ChatMessageRoles.isAssistant(role)) {
                        messages.add(new AssistantMessage(message.getContent()));
                    }
                });
        return messages;
    }

    @Override
    @Transactional
    public void save(String sessionId, List<Message> messages) {
        messages = ListUtils.emptyIfNull(messages).stream()
                .filter(Objects::nonNull)
                .filter(message -> MessageType.USER.equals(message.getMessageType()) ||
                        MessageType.ASSISTANT.equals(message.getMessageType()))
                .toList();
        messages = messages.stream()
                .skip(Math.max(0, messages.size() - MAX_MESSAGES))
                .toList();
        this.repository.deleteBySessionId(sessionId);
        AtomicInteger order = new AtomicInteger(0);
        messages.stream().forEach(message -> {
            ChatMessageEntity entity = ChatMessageEntity.builder()
                    .sessionId(sessionId)
                    .role(ChatMessageRoles.fromMessageType(message.getMessageType()))
                    .content(message.getText())
                    .sortOrder(order.getAndIncrement())
                    .createdAt(Instant.now())
                    .build();
            repository.save(entity);
        });
    }

    @Override
    @Transactional
    public void clear(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        this.repository.deleteBySessionId(sessionId);
    }
}
