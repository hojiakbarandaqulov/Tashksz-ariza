package org.example.toshkszariza.telegram;

import org.example.toshkszariza.domain.ConversationStep;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BotTextsTest {

    @Test
    void applicationPromptHasRequestedHeadingWithoutThreeLineInstruction() {
        String prompt = BotTexts.prompt(ConversationStep.WAITING_APPLICATION_DETAILS);

        assertThat(prompt)
                .startsWith("🏢 <b>Kompaniya nomi, telefon va arizani yozing:</b>")
                .doesNotContain("+998", "3 qator", "Asia polymer");
    }
}
