package com.webest.numerology.service;

import com.webest.numerology.repository.ChatConversationRepository;
import com.webest.numerology.repository.UserRepository;
import com.webest.numerology.repository.entity.ChatConversationEntity;
import com.webest.numerology.store.ChatHistoryStore;
import com.webest.numerology.web.dto.ConversationDto;
import com.webest.numerology.web.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ChatConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatHistoryStore historyStore;

    public ConversationService(
            ChatConversationRepository conversationRepository,
            UserRepository userRepository,
            ChatHistoryStore historyStore) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.historyStore = historyStore;
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> listForUser(Long userId) {
        return conversationRepository.findByUser_IdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ConversationDto create(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "使用者不存在"));
        Instant now = Instant.now();
        ChatConversationEntity entity = ChatConversationEntity.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .user(user)
                .title("新會話")
                .createdAt(now)
                .updatedAt(now)
                .build();
        conversationRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public void delete(Long userId, String conversationId) {
        ChatConversationEntity conversation = requireOwned(userId, conversationId);
        historyStore.clear(conversation.getId());
        conversationRepository.delete(conversation);
    }

    @Transactional
    public ChatConversationEntity requireOwned(Long userId, String conversationId) {
        if (StringUtils.isBlank(conversationId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "缺少會話 ID");
        }
        return conversationRepository.findByIdAndUser_Id(conversationId.trim(), userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "會話不存在"));
    }

    @Transactional
    public ConversationDto rename(Long userId, String conversationId, String title) {
        if (StringUtils.isBlank(title)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "請輸入會話名稱");
        }
        String trimmed = title.trim();
        if (trimmed.length() > 120) {
            trimmed = trimmed.substring(0, 120);
        }
        ChatConversationEntity conversation = requireOwned(userId, conversationId);
        conversation.setTitle(trimmed);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
        return toDto(conversation);
    }

    @Transactional
    public void touchAfterMessage(ChatConversationEntity conversation, String firstUserQuery) {
        conversation.setUpdatedAt(Instant.now());
        if ("新會話".equals(conversation.getTitle()) && StringUtils.isNotBlank(firstUserQuery)) {
            String title = firstUserQuery.trim();
            if (title.length() > 40) {
                title = title.substring(0, 40) + "…";
            }
            conversation.setTitle(title);
        }
        conversationRepository.save(conversation);
    }

    private ConversationDto toDto(ChatConversationEntity entity) {
        return ConversationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
