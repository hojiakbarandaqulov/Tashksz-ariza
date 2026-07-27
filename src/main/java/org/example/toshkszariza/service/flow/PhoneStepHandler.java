package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;
import org.example.toshkszariza.service.InputValidator;
import org.springframework.stereotype.Component;

@Component
public class PhoneStepHandler implements ConversationStepHandler {
    private final InputValidator validator;

    public PhoneStepHandler(InputValidator validator) {
        this.validator = validator;
    }

    @Override
    public ConversationStep supportedStep() {
        return ConversationStep.WAITING_PHONE;
    }

    @Override
    public StepResult handle(UserConversation conversation, StepInput input) {
        var phone = validator.normalizePhone(input.phoneValue());
        if (phone.isEmpty()) {
            return StepResult.error(supportedStep(), "Telefon raqamingizni yozib yuboring.");
        }
        conversation.setPhone(phone.get());
        if (conversation.getRegion() == null) {
            conversation.moveTo(ConversationStep.WAITING_REGION);
        } else if (conversation.getOrganizationName() == null) {
            conversation.moveTo(ConversationStep.WAITING_APPLICATION_DETAILS);
        } else if (conversation.getDescription() == null) {
            conversation.moveTo(ConversationStep.WAITING_DESCRIPTION);
        } else {
            conversation.moveTo(ConversationStep.CONFIRMING);
        }
        return StepResult.success(conversation.getStep());
    }
}
