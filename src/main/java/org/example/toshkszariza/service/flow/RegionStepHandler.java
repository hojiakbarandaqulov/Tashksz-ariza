package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ApplicationRegion;
import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;
import org.springframework.stereotype.Component;

@Component
public class RegionStepHandler implements ConversationStepHandler {
    @Override
    public ConversationStep supportedStep() {
        return ConversationStep.WAITING_REGION;
    }

    @Override
    public StepResult handle(UserConversation conversation, StepInput input) {
        var region = ApplicationRegion.fromLabel(input.text());
        if (region.isEmpty()) {
            return StepResult.error(supportedStep(), "Hududni pastdagi KSZ tugmalaridan tanlang.");
        }
        conversation.setRegion(region.get());
        conversation.moveTo(ConversationStep.WAITING_APPLICATION_DETAILS);
        return StepResult.success(conversation.getStep());
    }
}
