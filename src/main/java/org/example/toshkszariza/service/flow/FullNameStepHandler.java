package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;
import org.example.toshkszariza.service.InputValidator;
import org.springframework.stereotype.Component;

@Component
public class FullNameStepHandler implements ConversationStepHandler {
    private final InputValidator validator;

    public FullNameStepHandler(InputValidator validator) {
        this.validator = validator;
    }

    @Override
    public ConversationStep supportedStep() {
        return ConversationStep.WAITING_FULL_NAME;
    }

    @Override
    public StepResult handle(UserConversation conversation, StepInput input) {
        var error = validator.validateFullName(input.text());
        if (error.isPresent()) {
            return StepResult.error(supportedStep(), error.get());
        }
        conversation.setFullName(validator.clean(input.text()));
        conversation.moveTo(ConversationStep.WAITING_REGION);
        return StepResult.success(conversation.getStep());
    }
}
