package org.example.toshkszariza.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputValidatorTest {
    private final InputValidator validator = new InputValidator();

    @Test
    void normalizesUzbekPhoneNumber() {
        assertThat(validator.normalizeUzbekPhone("+998 (90) 123-45-67"))
                .contains("+998 90 123 45 67");
        assertThat(validator.normalizeUzbekPhone("90 123 45 67"))
                .contains("+998 90 123 45 67");
    }

    @Test
    void rejectsForeignOrShortPhoneNumber() {
        assertThat(validator.normalizeUzbekPhone("+7 999 123 45 67")).isEmpty();
        assertThat(validator.normalizeUzbekPhone("99890")).isEmpty();
    }

    @Test
    void requiresAtLeastFirstAndLastName() {
        assertThat(validator.validateFullName("Ali Valiyev")).isEmpty();
        assertThat(validator.validateFullName("Ali")).isPresent();
    }
}
