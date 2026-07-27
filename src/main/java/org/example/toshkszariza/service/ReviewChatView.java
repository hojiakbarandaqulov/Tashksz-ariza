package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.ReviewChatConfiguration;

public record ReviewChatView(
        long chatId,
        String chatType,
        String title,
        String username,
        Integer messageThreadId,
        Integer directMessagesTopicId
) {
    public static ReviewChatView from(ReviewChatConfiguration configuration) {
        return new ReviewChatView(
                configuration.getChatId(),
                configuration.getChatType(),
                configuration.getTitle(),
                configuration.getUsername(),
                configuration.getMessageThreadId(),
                configuration.getDirectMessagesTopicId()
        );
    }
}
