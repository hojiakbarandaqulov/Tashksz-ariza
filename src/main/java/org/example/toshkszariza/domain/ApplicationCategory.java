package org.example.toshkszariza.domain;

import java.util.Arrays;
import java.util.Optional;

public enum ApplicationCategory {
    TECHNICAL_TERMS("Texnik shart"),
    EMERGENCY("Avariya"),
    METER("Hisoblagich"),
    CONNECTION("Tarmoqqa ulanish"),
    OTHER("Boshqa");

    private final String label;

    ApplicationCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Optional<ApplicationCategory> fromLabel(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(category -> category.label.equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
