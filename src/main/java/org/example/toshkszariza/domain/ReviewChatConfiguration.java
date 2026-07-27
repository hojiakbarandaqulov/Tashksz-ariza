package org.example.toshkszariza.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Barcha yangi arizalar yuboriladigan bitta guruh/kanal manzilini saqlaydi. */
@Entity
@Table(name = "review_chat_configuration")
public class ReviewChatConfiguration {
    private static final int SINGLE_SLOT = 1;

    @Id
    @Column(name = "slot_id")
    private Integer slotId;

    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name = "chat_type", nullable = false, length = 20)
    private String chatType;

    @Column(length = 255)
    private String title;

    @Column(length = 64)
    private String username;

    @Column(name = "message_thread_id")
    private Integer messageThreadId;

    @Column(name = "direct_messages_topic_id")
    private Integer directMessagesTopicId;

    @Column(name = "updated_by", nullable = false)
    private long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReviewChatConfiguration() {
    }

    public ReviewChatConfiguration(
            long chatId,
            String chatType,
            String title,
            String username,
            Integer messageThreadId,
            Integer directMessagesTopicId,
            long updatedBy
    ) {
        this.slotId = SINGLE_SLOT;
        this.chatId = chatId;
        this.chatType = chatType;
        this.title = title;
        this.username = username;
        this.messageThreadId = messageThreadId;
        this.directMessagesTopicId = directMessagesTopicId;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public long getChatId() { return chatId; }
    public String getChatType() { return chatType; }
    public String getTitle() { return title; }
    public String getUsername() { return username; }
    public Integer getMessageThreadId() { return messageThreadId; }
    public Integer getDirectMessagesTopicId() { return directMessagesTopicId; }
}
