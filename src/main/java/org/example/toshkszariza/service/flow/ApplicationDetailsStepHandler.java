package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;
import org.example.toshkszariza.service.InputValidator;
import org.springframework.stereotype.Component;

/** Korxona nomi, telefon va ariza tavsifini foydalanuvchining bitta xabaridan ajratadi. */
@Component
public class ApplicationDetailsStepHandler implements ConversationStepHandler {
    private final InputValidator validator;

    public ApplicationDetailsStepHandler(InputValidator validator) {
        this.validator = validator;
    }

    @Override
    public ConversationStep supportedStep() {
        return ConversationStep.WAITING_APPLICATION_DETAILS;
    }

    @Override
    public StepResult handle(UserConversation conversation, StepInput input) {
        if (input.hasAttachment()) {
            return handleAttachment(conversation, input);
        }
        String rawText = input.text() == null ? "" : input.text().trim();
        String[] parts = rawText.split("\\R", 3);
        if (parts.length < 2) {
            return StepResult.error(supportedStep(),
                    "Korxona nomi, telefon va arizani alohida qatorlarda yozing.");
        }

        String organization = removePrefix(parts[0], "korxona nomi:", "korxona:", "kompaniya:");
        String phone = null;
        String description;
        if (parts.length == 3) {
            phone = normalizePhone(parts[1]);
            if (phone == null) {
                return StepResult.error(supportedStep(),
                        "Telefonni +998 90 123 45 67 ko'rinishida ikkinchi qatorga yozing.");
            }
            description = removePrefix(parts[2], "tavsif:", "ariza:", "ariza matni:");
        } else {
            // Eski 2 qatorli format ham buzilmaydi; telefon keyingi bosqichda alohida so'raladi.
            description = removePrefix(parts[1], "tavsif:", "ariza:", "ariza matni:");
        }

        var organizationError = validator.validateOrganization(organization);
        if (organizationError.isPresent()) {
            return StepResult.error(supportedStep(), organizationError.get());
        }
        var descriptionError = validator.validateDescription(description);
        if (descriptionError.isPresent()) {
            return StepResult.error(supportedStep(), descriptionError.get());
        }

        conversation.setOrganizationName(validator.clean(organization));
        if (phone != null) {
            conversation.setPhone(phone);
        }
        conversation.setDescription(validator.clean(description));
        conversation.moveTo(conversation.getPhone() == null
                ? ConversationStep.WAITING_PHONE
                : ConversationStep.CONFIRMING);
        return StepResult.success(conversation.getStep());
    }

    private StepResult handleAttachment(UserConversation conversation, StepInput input) {
        conversation.setAttachment(input.attachmentType(), input.attachmentFileId());
        String caption = input.text() == null ? "" : input.text().trim();
        conversation.setDescription(input.attachmentType().defaultDescription());

        if (caption.isBlank()) {
            conversation.moveTo(ConversationStep.WAITING_ORGANIZATION);
            return StepResult.success(conversation.getStep());
        }

        String[] parts = caption.split("\\R", 3);
        String organization = removePrefix(parts[0], "korxona nomi:", "korxona:", "kompaniya:");
        String phone = null;
        String description = input.attachmentType().defaultDescription();
        if (parts.length == 3) {
            phone = normalizePhone(parts[1]);
            if (phone == null) {
                return StepResult.error(supportedStep(),
                        "Telefonni +998 90 123 45 67 ko'rinishida ikkinchi qatorga yozing.");
            }
            description = removePrefix(parts[2], "tavsif:", "ariza:", "ariza matni:");
        } else if (parts.length == 2) {
            phone = normalizePhone(parts[1]);
            if (phone == null) {
                description = removePrefix(parts[1], "tavsif:", "ariza:", "ariza matni:");
            }
        }

        var organizationError = validator.validateOrganization(organization);
        if (organizationError.isPresent()) {
            return StepResult.error(supportedStep(), organizationError.get());
        }
        var descriptionError = validator.validateDescription(description);
        if (descriptionError.isPresent()) {
            return StepResult.error(supportedStep(), descriptionError.get());
        }

        conversation.setOrganizationName(validator.clean(organization));
        if (phone != null) {
            conversation.setPhone(phone);
        }
        conversation.setDescription(validator.clean(description));
        conversation.moveTo(conversation.getPhone() == null
                ? ConversationStep.WAITING_PHONE
                : ConversationStep.CONFIRMING);
        return StepResult.success(conversation.getStep());
    }

    private String normalizePhone(String value) {
        String withoutPrefix = removePrefix(value, "telefon:", "tel:", "raqam:", "nomer:");
        return validator.normalizeUzbekPhone(withoutPrefix).orElse(null);
    }

    private String removePrefix(String value, String... prefixes) {
        String trimmed = value.trim();
        String lowerCase = trimmed.toLowerCase(java.util.Locale.ROOT);
        for (String prefix : prefixes) {
            if (lowerCase.startsWith(prefix)) {
                return trimmed.substring(prefix.length()).trim();
            }
        }
        return trimmed;
    }
}
