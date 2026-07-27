package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;
import org.example.toshkszariza.service.InputValidator;
import org.springframework.stereotype.Component;

@Component
public class OrganizationStepHandler implements ConversationStepHandler {
    private final InputValidator validator;

    public OrganizationStepHandler(InputValidator validator) {
        this.validator = validator;
    }

    @Override
    public ConversationStep supportedStep() {
        return ConversationStep.WAITING_ORGANIZATION;
    }

    @Override
    public StepResult handle(UserConversation conversation, StepInput input) {
        var error = validator.validateOrganization(input.text());
        if (error.isPresent()) {
            return StepResult.error(supportedStep(), error.get());
        }
        conversation.setOrganizationName(validator.clean(input.text()));
        // Captionsiz media (ayniqsa video-xabar) yuborilganda tavsif media turidan tayyor bo'ladi.
        if (conversation.getAttachmentType() != null && conversation.getDescription() != null) {
            conversation.moveTo(ConversationStep.CONFIRMING);
        } else {
            conversation.moveTo(ConversationStep.WAITING_DESCRIPTION);
        }
        return StepResult.success(conversation.getStep());
    }
}
