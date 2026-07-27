package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.ConversationStep;

public enum DraftField {
    FULL_NAME("fio", ConversationStep.WAITING_FULL_NAME),
    PHONE("phone", ConversationStep.WAITING_PHONE),
    ORGANIZATION("organization", ConversationStep.WAITING_ORGANIZATION),
    APPLICATION_DETAILS("details", ConversationStep.WAITING_APPLICATION_DETAILS),
    REGION("region", ConversationStep.WAITING_REGION),
    CATEGORY("category", ConversationStep.WAITING_CATEGORY),
    DESCRIPTION("description", ConversationStep.WAITING_DESCRIPTION);

    private final String callbackKey;
    private final ConversationStep step;

    DraftField(String callbackKey, ConversationStep step) {
        this.callbackKey = callbackKey;
        this.step = step;
    }

    public String callbackKey() { return callbackKey; }
    public ConversationStep step() { return step; }

    public static DraftField fromCallbackKey(String key) {
        for (DraftField field : values()) {
            if (field.callbackKey.equals(key)) {
                return field;
            }
        }
        throw new BotBusinessException("Noma'lum tahrirlash maydoni.");
    }
}
