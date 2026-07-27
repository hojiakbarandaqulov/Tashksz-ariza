package org.example.toshkszariza.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Admin yozayotgan umumiy xabarni tasdiqlashgacha bazada saqlaydi. */
@Entity
@Table(name = "admin_broadcast_sessions")
public class AdminBroadcastSession {
    @Id
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "message_text", length = 3500)
    private String messageText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AdminBroadcastSession() {
    }

    public AdminBroadcastSession(long adminId) {
        this.adminId = adminId;
        this.createdAt = Instant.now();
    }

    public void prepare(String messageText) {
        this.messageText = messageText;
    }

    public boolean isPrepared() {
        return messageText != null && !messageText.isBlank();
    }

    public Long getAdminId() { return adminId; }
    public String getMessageText() { return messageText; }
}
