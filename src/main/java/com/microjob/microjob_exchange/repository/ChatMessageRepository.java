package com.microjob.microjob_exchange.repository;

import com.microjob.microjob_exchange.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Retrieve chat history for a specific task
    List<ChatMessage> findByTaskIdOrderBySentAtAsc(Long taskId);
}