package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;
import org.example.toshkszariza.service.InputValidator;
import org.springframework.stereotype.Component;

@Component
public class DescriptionStepHandler implements ConversationStepHandler {
    private final InputValidator validator;

    public DescriptionStepHandler(InputValidator validator) {
        this.validator = validator;
    }

    @Override
    public ConversationStep supportedStep() {
        return ConversationStep.WAITING_DESCRIPTION;
    }

    @Override
    public StepResult handle(UserConversation conversation, StepInput input) {
        var error = validator.validateDescription(input.text());
        if (error.isPresent()) {
            return StepResult.error(supportedStep(), error.get());
        }
        conversation.setDescription(validator.clean(input.text()));
        conversation.moveTo(conversation.getPhone() == null
                ? ConversationStep.WAITING_PHONE
                : ConversationStep.CONFIRMING);
        return StepResult.success(conversation.getStep());
    }
}
