package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.ApplicationCategory;
import org.example.toshkszariza.domain.ApplicationAttachmentType;
import org.example.toshkszariza.domain.ApplicationRegion;
import org.example.toshkszariza.domain.ApplicationStatus;
import org.example.toshkszariza.domain.ServiceApplication;

import java.time.Instant;

public record ApplicationView(
        long id,
        long telegramUserId,
        long userChatId,
        Integer userMessageThreadId,
        Integer userDirectMessagesTopicId,
        String telegramUsername,
        String fullName,
        String phone,
        String organizationName,
        ApplicationRegion region,
        ApplicationCategory category,
        String description,
        ApplicationAttachmentType attachmentType,
        String attachmentFileId,
        ApplicationStatus status,
        String rejectionReason,
        int revision,
        Instant submittedAt
) {
    public static ApplicationView from(ServiceApplication application) {
        return new ApplicationView(
                application.getId(),
                application.getTelegramUserId(),
                application.getUserChatId(),
                application.getUserMessageThreadId(),
                application.getUserDirectMessagesTopicId(),
                application.getTelegramUsername(),
                application.getFullName(),
                application.getPhone(),
                application.getOrganizationName(),
                application.getRegion(),
                application.getCategory(),
                application.getDescription(),
                application.getAttachmentType(),
                application.getAttachmentFileId(),
                application.getStatus(),
                application.getRejectionReason(),
                application.getRevision(),
                application.getSubmittedAt()
        );
    }
}
