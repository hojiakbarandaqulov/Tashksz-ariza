package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ConversationStep;

public record StepResult(boolean accepted, ConversationStep currentStep, String errorMessage) {
    public static StepResult success(ConversationStep nextStep) {
        return new StepResult(true, nextStep, null);
    }

    public static StepResult error(ConversationStep currentStep, String message) {
        return new StepResult(false, currentStep, message);
    }
}
