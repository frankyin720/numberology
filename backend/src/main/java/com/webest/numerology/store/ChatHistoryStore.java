package com.webest.numerology.store;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface ChatHistoryStore {

    List<Message> load(String sessionId);

    void save(String sessionId, List<Message> messages);

    void clear(String sessionId);
}
