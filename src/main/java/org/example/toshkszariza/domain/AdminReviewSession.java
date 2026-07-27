package org.example.toshkszariza.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_review_sessions")
public class AdminReviewSession {
    @Id
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "notification_chat_id", nullable = false)
    private long notificationChatId;

    @Column(name = "notification_message_id", nullable = false)
    private int notificationMessageId;

    protected AdminReviewSession() {
    }

    public AdminReviewSession(long adminId, long applicationId, long notificationChatId, int notificationMessageId) {
        this.adminId = adminId;
        this.applicationId = applicationId;
        this.notificationChatId = notificationChatId;
        this.notificationMessageId = notificationMessageId;
    }

    public Long getAdminId() { return adminId; }
    public Long getApplicationId() { return applicationId; }
    public long getNotificationChatId() { return notificationChatId; }
    public int getNotificationMessageId() { return notificationMessageId; }
}
