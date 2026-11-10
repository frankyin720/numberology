package com.webest.numerology.repository;

import com.webest.numerology.repository.entity.ChatConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversationEntity, String> {

    List<ChatConversationEntity> findByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<ChatConversationEntity> findByIdAndUser_Id(String id, Long userId);
}
