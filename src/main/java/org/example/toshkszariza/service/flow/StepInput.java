package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ApplicationAttachmentType;

public record StepInput(
        String text,
        String contactPhone,
        ApplicationAttachmentType attachmentType,
        String attachmentFileId
) {
    public StepInput(String text, String contactPhone) {
        this(text, contactPhone, null, null);
    }

    public String phoneValue() {
        return contactPhone == null || contactPhone.isBlank() ? text : contactPhone;
    }

    public boolean hasAttachment() {
        return attachmentType != null && attachmentFileId != null && !attachmentFileId.isBlank();
    }
}
