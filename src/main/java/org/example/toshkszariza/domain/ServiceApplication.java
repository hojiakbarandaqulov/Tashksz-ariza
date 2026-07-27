package org.example.toshkszariza.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "service_applications", indexes = {
        @Index(name = "idx_application_user_created", columnList = "telegram_user_id,created_at"),
        @Index(name = "idx_application_status_submitted", columnList = "status,submitted_at")
})
public class ServiceApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(name = "telegram_user_id", nullable = false)
    private long telegramUserId;

    @Column(name = "user_chat_id", nullable = false)
    private long userChatId;

    @Column(name = "user_message_thread_id")
    private Integer userMessageThreadId;

    @Column(name = "user_direct_messages_topic_id")
    private Integer userDirectMessagesTopicId;

    @Column(name = "telegram_username", length = 64)
    private String telegramUsername;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "organization_name", nullable = false, length = 150)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    // Eski arizalarda hudud bo'lmagan; yangi oqim esa service qatlamida hududni majburiy qiladi.
    @Column(length = 40)
    private ApplicationRegion region;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ApplicationCategory category;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", length = 20)
    private ApplicationAttachmentType attachmentType;

    @Column(name = "attachment_file_id", length = 512)
    private String attachmentFileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(nullable = false)
    private int revision;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected ServiceApplication() {
    }

    public static ServiceApplication create(UserConversation draft, String telegramUsername) {
        ServiceApplication application = new ServiceApplication();
        application.telegramUserId = draft.getTelegramUserId();
        application.userChatId = draft.getActiveChatId();
        application.userMessageThreadId = draft.getMessageThreadId();
        application.userDirectMessagesTopicId = draft.getDirectMessagesTopicId();
        application.telegramUsername = telegramUsername;
        application.copyDraft(draft);
        application.status = ApplicationStatus.PENDING;
        application.revision = 1;
        application.createdAt = Instant.now();
        application.submittedAt = application.createdAt;
        return application;
    }

    /** Rad etilgan arizaning o'zini yangi tahrir sifatida qayta navbatga qo'yadi. */
    public void resubmit(UserConversation draft, String telegramUsername) {
        if (status != ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Faqat qaytarilgan arizani qayta yuborish mumkin");
        }
        this.telegramUsername = telegramUsername;
        copyDraft(draft);
        status = ApplicationStatus.PENDING;
        rejectionReason = null;
        reviewedBy = null;
        reviewedAt = null;
        submittedAt = Instant.now();
        revision++;
    }

    public void accept(long adminId) {
        ensurePending();
        status = ApplicationStatus.ACCEPTED;
        reviewedBy = adminId;
        reviewedAt = Instant.now();
    }

    public void reject(long adminId, String reason) {
        ensurePending();
        status = ApplicationStatus.REJECTED;
        reviewedBy = adminId;
        rejectionReason = reason;
        reviewedAt = Instant.now();
    }

    private void copyDraft(UserConversation draft) {
        userChatId = draft.getActiveChatId();
        userMessageThreadId = draft.getMessageThreadId();
        userDirectMessagesTopicId = draft.getDirectMessagesTopicId();
        fullName = draft.getFullName();
        phone = draft.getPhone();
        organizationName = draft.getOrganizationName();
        region = draft.getRegion();
        category = draft.getCategory();
        description = draft.getDescription();
        attachmentType = draft.getAttachmentType();
        attachmentFileId = draft.getAttachmentFileId();
    }

    private void ensurePending() {
        if (status != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Ariza allaqachon ko'rib chiqilgan");
        }
    }

    public Long getId() { return id; }
    public long getTelegramUserId() { return telegramUserId; }
    public long getUserChatId() { return userChatId; }
    public Integer getUserMessageThreadId() { return userMessageThreadId; }
    public Integer getUserDirectMessagesTopicId() { return userDirectMessagesTopicId; }
    public String getTelegramUsername() { return telegramUsername; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getOrganizationName() { return organizationName; }
    public ApplicationRegion getRegion() { return region; }
    public ApplicationCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public ApplicationAttachmentType getAttachmentType() { return attachmentType; }
    public String getAttachmentFileId() { return attachmentFileId; }
    public ApplicationStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public int getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
}
