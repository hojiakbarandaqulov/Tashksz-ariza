package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ApplicationCategory;
import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;
import org.springframework.stereotype.Component;

@Component
public class CategoryStepHandler implements ConversationStepHandler {
    @Override
    public ConversationStep supportedStep() {
        return ConversationStep.WAITING_CATEGORY;
    }

    @Override
    public StepResult handle(UserConversation conversation, StepInput input) {
        var category = ApplicationCategory.fromLabel(input.text());
        if (category.isEmpty()) {
            return StepResult.error(supportedStep(), "Kategoriyani pastdagi tugmalardan tanlang.");
        }
        conversation.setCategory(category.get());
        conversation.moveTo(ConversationStep.WAITING_DESCRIPTION);
        return StepResult.success(conversation.getStep());
    }
}
