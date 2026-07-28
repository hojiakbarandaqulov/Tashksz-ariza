package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;
import org.example.toshkszariza.service.InputValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Korxona, ariza va telefonni qat'iy qator soniga bog'lamasdan qabul qiladi. */
@Component
public class ApplicationDetailsStepHandler implements ConversationStepHandler {
    private static final String ORGANIZATION_NOT_PROVIDED = "Ko'rsatilmagan";
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

        List<String> parts = flexibleParts(input.text());
        if (parts.isEmpty()) {
            return StepResult.error(supportedStep(), "Arizangizni yozib yuboring.");
        }

        // Bitta oddiy xabar tavsif sifatida saqlanadi, telefon esa keyingi majburiy bosqichda so'raladi.
        if (parts.size() == 1) {
            return savePlainApplication(conversation, parts.get(0));
        }

        StepResult organizationResult = saveOrganization(conversation, parts.get(0));
        if (organizationResult != null) {
            return organizationResult;
        }

        saveOptionalDescriptionAndPhone(conversation, parts.subList(1, parts.size()), null);
        return advance(conversation);
    }

    private StepResult handleAttachment(UserConversation conversation, StepInput input) {
        conversation.setAttachment(input.attachmentType(), input.attachmentFileId());
        String defaultDescription = input.attachmentType().defaultDescription();
        conversation.setDescription(defaultDescription);

        List<String> parts = flexibleParts(input.text());
        if (parts.isEmpty()) {
            conversation.setOrganizationName(ORGANIZATION_NOT_PROVIDED);
            return advance(conversation);
        }

        // Media izohidagi bitta matn ham tayyor ariza sifatida qabul qilinadi.
        if (parts.size() == 1) {
            conversation.setOrganizationName(ORGANIZATION_NOT_PROVIDED);
            String description = removePrefix(parts.get(0), "tavsif:", "ariza:", "ariza matni:");
            if (validator.validateDescription(description).isEmpty()) {
                conversation.setDescription(validator.clean(description));
            }
            return advance(conversation);
        }

        StepResult organizationResult = saveOrganization(conversation, parts.get(0));
        if (organizationResult != null) {
            return organizationResult;
        }

        saveOptionalDescriptionAndPhone(conversation, parts.subList(1, parts.size()), defaultDescription);
        return advance(conversation);
    }

    /** Qolgan satrlardan telefonni topadi, boshqa satrlarni esa bitta tavsifga birlashtiradi. */
    private void saveOptionalDescriptionAndPhone(
            UserConversation conversation,
            List<String> details,
            String defaultDescription
    ) {
        int phoneIndex = findPhoneIndex(details);
        if (phoneIndex >= 0) {
            conversation.setPhone(normalizePhone(details.get(phoneIndex)));
        }

        List<String> descriptionParts = new ArrayList<>();
        for (int index = 0; index < details.size(); index++) {
            if (index != phoneIndex) {
                descriptionParts.add(details.get(index));
            }
        }

        if (!descriptionParts.isEmpty()) {
            String description = removePrefix(
                    String.join(" ", descriptionParts),
                    "tavsif:", "ariza:", "ariza matni:"
            );
            if (validator.validateDescription(description).isEmpty()) {
                conversation.setDescription(validator.clean(description));
            }
        } else if (defaultDescription != null) {
            conversation.setDescription(defaultDescription);
        } else {
            conversation.setDescription(null);
        }
    }

    private int findPhoneIndex(List<String> details) {
        // Telefon odatda oxirida yoziladi; eski format uchun boshqa satrlar ham tekshiriladi.
        for (int index = details.size() - 1; index >= 0; index--) {
            if (normalizePhone(details.get(index)) != null) {
                return index;
            }
        }
        return -1;
    }

    private StepResult saveOrganization(UserConversation conversation, String value) {
        String organization = removePrefix(value, "korxona nomi:", "korxona:", "kompaniya:");
        var error = validator.validateOrganization(organization);
        if (error.isPresent()) {
            return StepResult.error(supportedStep(), error.get());
        }
        conversation.setOrganizationName(validator.clean(organization));
        return null;
    }

    private StepResult advance(UserConversation conversation) {
        if (conversation.getDescription() == null) {
            conversation.moveTo(ConversationStep.WAITING_DESCRIPTION);
        } else if (conversation.getPhone() == null || conversation.getPhone().isBlank()) {
            conversation.moveTo(ConversationStep.WAITING_PHONE);
        } else {
            conversation.moveTo(ConversationStep.CONFIRMING);
        }
        return StepResult.success(conversation.getStep());
    }

    private StepResult savePlainApplication(UserConversation conversation, String value) {
        String description = removePrefix(value, "tavsif:", "ariza:", "ariza matni:");
        var error = validator.validateDescription(description);
        if (error.isPresent()) {
            return StepResult.error(supportedStep(), error.get());
        }
        conversation.setOrganizationName(ORGANIZATION_NOT_PROVIDED);
        conversation.setDescription(validator.clean(description));
        return advance(conversation);
    }

    /** Yangi qator majburiy emas: nuqtali vergul va | belgisi ham ajratuvchi bo'la oladi. */
    private List<String> flexibleParts(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("(?:\\R|[;|])+"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private String normalizePhone(String value) {
        String withoutPrefix = removePrefix(value, "telefon:", "tel:", "raqam:", "nomer:");
        return validator.normalizePhone(withoutPrefix).orElse(null);
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
