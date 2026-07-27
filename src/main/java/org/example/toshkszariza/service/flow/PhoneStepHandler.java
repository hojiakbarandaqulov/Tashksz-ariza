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
        var phone = validator.normalizeUzbekPhone(input.phoneValue());
        if (phone.isEmpty()) {
            return StepResult.error(supportedStep(), "Telefonni +998 90 123 45 67 ko'rinishida yuboring.");
        }
        conversation.setPhone(phone.get());
        conversation.moveTo(ConversationStep.WAITING_REGION);
        return StepResult.success(conversation.getStep());
    }
}
