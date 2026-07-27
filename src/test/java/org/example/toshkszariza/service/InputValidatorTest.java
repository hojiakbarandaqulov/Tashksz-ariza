package org.example.toshkszariza.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputValidatorTest {
    private final InputValidator validator = new InputValidator();

    @Test
    void acceptsPhoneWithoutForcingUzbekPrefix() {
        assertThat(validator.normalizePhone("+998 (90) 123-45-67"))
                .contains("+998 (90) 123-45-67");
        assertThat(validator.normalizePhone("90 123 45 67"))
                .contains("90 123 45 67");
        assertThat(validator.normalizePhone("+7 999 123 45 67"))
                .contains("+7 999 123 45 67");
    }

    @Test
    void rejectsTextWithoutAnyPhoneNumber() {
        assertThat(validator.normalizePhone("telefon yo'q")).isEmpty();
    }

    @Test
    void requiresAtLeastFirstAndLastName() {
        assertThat(validator.validateFullName("Ali Valiyev")).isEmpty();
        assertThat(validator.validateFullName("Ali")).isPresent();
    }
}
