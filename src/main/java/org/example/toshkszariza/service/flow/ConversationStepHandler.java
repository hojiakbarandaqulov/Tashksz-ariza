package org.example.toshkszariza.service.flow;

import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.UserConversation;

/** Har bir suhbat bosqichi ushbu shartnoma orqali mustaqil ishlaydi (Strategy pattern). */
public interface ConversationStepHandler {
    ConversationStep supportedStep();

    StepResult handle(UserConversation conversation, StepInput input);
}
