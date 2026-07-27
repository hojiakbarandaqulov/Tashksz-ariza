package org.example.toshkszariza.telegram;

import org.example.toshkszariza.domain.ConversationStep;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BotTextsTest {

    @Test
    void applicationPromptHasNoPhoneOrThreeLineInstruction() {
        String prompt = BotTexts.prompt(ConversationStep.WAITING_APPLICATION_DETAILS);

        assertThat(prompt)
                .contains("Arizangizni yozing")
                .doesNotContain("telefon", "+998", "3 qator", "Asia polymer");
    }
}
