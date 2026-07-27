package org.example.toshkszariza.repository;

import org.example.toshkszariza.domain.UserConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserConversationRepository extends JpaRepository<UserConversation, Long> {
    @Query("select distinct conversation.chatId from UserConversation conversation")
    List<Long> findRecipientChatIds();

    Optional<UserConversation> findFirstByTelegramUserId(long telegramUserId);
}
