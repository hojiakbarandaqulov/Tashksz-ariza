package org.example.toshkszariza.telegram;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BotKeyboardsTest {

    @Test
    void adminMenuDoesNotContainPendingApplicationsButton() {
        String menu = BotKeyboards.mainMenu(true, true).toString();

        assertThat(menu)
                .doesNotContain("Kutilayotgan arizalar")
                .contains("Xabar yuborish", "7 kunlik hisobot");
    }
}
