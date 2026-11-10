package com.webest.numerology.repository;

import com.webest.numerology.repository.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findBySessionIdOrderBySortOrderAsc(String sessionId);

    @Modifying
    @Transactional
    void deleteBySessionId(String sessionId);
}
