package org.example.toshkszariza.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_conversations")
public class UserConversation {
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "telegram_user_id", nullable = false)
    private long telegramUserId;

    @Column(name = "active_chat_id")
    private Long activeChatId;

    @Column(name = "private_chat_id")
    private Long privateChatId;

    @Column(name = "message_thread_id")
    private Integer messageThreadId;

    @Column(name = "direct_messages_topic_id")
    private Integer directMessagesTopicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ConversationStep step = ConversationStep.IDLE;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "organization_name", length = 150)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ApplicationRegion region;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ApplicationCategory category;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", length = 20)
    private ApplicationAttachmentType attachmentType;

    @Column(name = "attachment_file_id", length = 512)
    private String attachmentFileId;

    @Column(name = "editing_application_id")
    private Long editingApplicationId;

    @Column(name = "single_field_edit", nullable = false)
    private boolean singleFieldEdit;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserConversation() {
    }

    public UserConversation(long chatId, long telegramUserId) {
        // Shaxsiy chatda chatId=userId. Guruhda esa har bir user uchun alohida kalit kerak.
        this.chatId = telegramUserId;
        this.telegramUserId = telegramUserId;
        updateDestination(chatId, null, null, chatId == telegramUserId);
        this.updatedAt = Instant.now();
    }

    public void updateDestination(
            long activeChatId,
            Integer messageThreadId,
            Integer directMessagesTopicId,
            boolean privateChat
    ) {
        this.activeChatId = activeChatId;
        this.messageThreadId = messageThreadId;
        this.directMessagesTopicId = directMessagesTopicId;
        if (privateChat) {
            this.privateChatId = activeChatId;
        }
        this.updatedAt = Instant.now();
    }

    public void startNew() {
        fullName = null;
        phone = null;
        organizationName = null;
        category = null;
        description = null;
        attachmentType = null;
        attachmentFileId = null;
        editingApplicationId = null;
        singleFieldEdit = false;

        // Hudud bir marta tanlanadi. Eski, endi tanlanmaydigan hudud bo'lsa qayta so'raladi.
        if (region == null || !region.isSelectable()) {
            region = null;
            moveTo(ConversationStep.WAITING_REGION);
        } else {
            moveTo(ConversationStep.WAITING_APPLICATION_DETAILS);
        }
    }

    public void loadForCorrection(ServiceApplication application) {
        fullName = application.getFullName();
        phone = application.getPhone();
        organizationName = application.getOrganizationName();
        region = application.getRegion();
        category = application.getCategory();
        description = application.getDescription();
        attachmentType = application.getAttachmentType();
        attachmentFileId = application.getAttachmentFileId();
        editingApplicationId = application.getId();
        singleFieldEdit = false;
        moveTo(ConversationStep.CONFIRMING);
    }

    public void cancel() {
        fullName = null;
        phone = null;
        organizationName = null;
        category = null;
        description = null;
        attachmentType = null;
        attachmentFileId = null;
        editingApplicationId = null;
        singleFieldEdit = false;
        moveTo(ConversationStep.IDLE);
    }

    public void submitted() {
        cancel();
    }

    public void moveTo(ConversationStep newStep) {
        this.step = newStep;
        this.updatedAt = Instant.now();
    }

    public void beginSingleFieldEdit(ConversationStep fieldStep) {
        singleFieldEdit = true;
        moveTo(fieldStep);
    }

    public void finishSingleFieldEdit() {
        singleFieldEdit = false;
        moveTo(ConversationStep.CONFIRMING);
    }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public void setRegion(ApplicationRegion region) { this.region = region; }
    public void setCategory(ApplicationCategory category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setAttachment(ApplicationAttachmentType type, String fileId) {
        this.attachmentType = type;
        this.attachmentFileId = fileId;
    }

    /** Admin qo'shish va shaxsiy xabarlar uchun avval private chat qaytariladi. */
    public Long getChatId() { return privateChatId != null ? privateChatId : getActiveChatId(); }
    public Long getActiveChatId() { return activeChatId != null ? activeChatId : chatId; }
    public Integer getMessageThreadId() { return messageThreadId; }
    public Integer getDirectMessagesTopicId() { return directMessagesTopicId; }
    public long getTelegramUserId() { return telegramUserId; }
    public ConversationStep getStep() { return step; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getOrganizationName() { return organizationName; }
    public ApplicationRegion getRegion() { return region; }
    public ApplicationCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public ApplicationAttachmentType getAttachmentType() { return attachmentType; }
    public String getAttachmentFileId() { return attachmentFileId; }
    public Long getEditingApplicationId() { return editingApplicationId; }
    public boolean isSingleFieldEdit() { return singleFieldEdit; }
}
