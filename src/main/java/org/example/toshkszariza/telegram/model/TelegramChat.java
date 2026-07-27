package org.example.toshkszariza.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramChat(long id, String type, String title, String username) {
    public boolean isPrivate() {
        return "private".equals(type);
    }

    public boolean supportsUserMessages() {
        return isPrivate() || "group".equals(type) || "supergroup".equals(type);
    }
}
