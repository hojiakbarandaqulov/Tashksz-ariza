package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.ApplicationCategory;
import org.example.toshkszariza.domain.ApplicationAttachmentType;
import org.example.toshkszariza.domain.ApplicationRegion;

public record DraftView(
        String fullName,
        String phone,
        String organizationName,
        ApplicationRegion region,
        ApplicationCategory category,
        String description,
        ApplicationAttachmentType attachmentType,
        String attachmentFileId,
        Long editingApplicationId
) {
}
