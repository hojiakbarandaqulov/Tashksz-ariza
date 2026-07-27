package org.example.toshkszariza.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessage(
        @JsonProperty("message_id") int messageId,
        @JsonProperty("message_thread_id") Integer messageThreadId,
        TelegramChat chat,
        TelegramUser from,
        String text,
        String caption,
        TelegramContact contact,
        List<TelegramPhotoSize> photo,
        TelegramVideo video,
        @JsonProperty("video_note") TelegramVideoNote videoNote,
        @JsonProperty("direct_messages_topic") TelegramDirectMessagesTopic directMessagesTopic
) {
}
