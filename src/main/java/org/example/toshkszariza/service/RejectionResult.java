package org.example.toshkszariza.service;

public record RejectionResult(
        ApplicationView application,
        long notificationChatId,
        int notificationMessageId
) {
}
