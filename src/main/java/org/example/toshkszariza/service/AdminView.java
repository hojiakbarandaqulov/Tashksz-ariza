package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.BotAdmin;

public record AdminView(long telegramUserId, long chatId, boolean superAdmin) {
    public static AdminView from(BotAdmin admin) {
        return new AdminView(admin.getTelegramUserId(), admin.getChatId(), admin.isSuperAdmin());
    }
}
