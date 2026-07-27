package org.example.toshkszariza.telegram.model;

/** Xabarni aynan o'sha guruh mavzusi yoki kanal Direct Messages mavzusiga qaytarish manzili. */
public record TelegramDestination(
        long chatId,
        Integer messageThreadId,
        Integer directMessagesTopicId
) {
    public static TelegramDestination from(TelegramMessage message) {
        Integer directTopicId = message.directMessagesTopic() == null
                ? null
                : message.directMessagesTopic().topicId();
        return new TelegramDestination(message.chat().id(), message.messageThreadId(), directTopicId);
    }
}
