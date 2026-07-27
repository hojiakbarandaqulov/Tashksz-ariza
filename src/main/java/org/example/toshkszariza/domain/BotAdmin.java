package org.example.toshkszariza.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Telegram username o'zgarishi mumkin, shu sabab admin raqamli user ID orqali aniqlanadi. */
@Entity
@Table(name = "bot_admin")
public class BotAdmin {
    @Id
    @Column(name = "slot_id")
    private Integer slotId;

    @Column(name = "telegram_user_id", nullable = false, unique = true)
    private long telegramUserId;

    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name = "telegram_username", length = 64)
    private String telegramUsername;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected BotAdmin() {
    }

    public BotAdmin(int slotId, long telegramUserId, long chatId, String telegramUsername) {
        this.slotId = slotId;
        this.telegramUserId = telegramUserId;
        this.chatId = chatId;
        this.telegramUsername = telegramUsername;
        this.createdAt = Instant.now();
    }

    public Integer getSlotId() { return slotId; }
    public long getTelegramUserId() { return telegramUserId; }
    public long getChatId() { return chatId; }
    public String getTelegramUsername() { return telegramUsername; }

    /** Birinchi slot egasi boshqa administratorlarni boshqaradigan bosh admin hisoblanadi. */
    public boolean isSuperAdmin() { return slotId != null && slotId == 1; }
}
