package org.example.toshkszariza.service;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InputValidator {
    public Optional<String> validateFullName(String value) {
        String text = clean(value);
        if (text.length() < 5 || text.length() > 120 || text.split("\\s+").length < 2) {
            return Optional.of("F.I.O. kamida ism va familiyadan iborat bo'lsin (5–120 belgi).");
        }
        return Optional.empty();
    }

    public Optional<String> validateOrganization(String value) {
        String text = clean(value);
        if (text.length() < 2 || text.length() > 150) {
            return Optional.of("Tashkilot nomi 2–150 belgi oralig'ida bo'lishi kerak.");
        }
        return Optional.empty();
    }

    public Optional<String> validateDescription(String value) {
        String text = clean(value);
        if (text.length() < 10 || text.length() > 2000) {
            return Optional.of("Tavsifni aniqroq yozing: 10–2000 belgi.");
        }
        return Optional.empty();
    }

    /** Telefonni +998 prefiksiga bog'lamasdan, bazaga xavfsiz uzunlikda tayyorlaydi. */
    public Optional<String> normalizePhone(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String phone = clean(value);
        if (phone.isBlank() || !phone.matches("[+()\\d.\\-\\s]+")) {
            return Optional.empty();
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return Optional.empty();
        }

        // Ustun varchar(20): uzun format bo'lsa faqat raqamlar va boshidagi '+' saqlanadi.
        if (phone.length() > 20) {
            phone = (phone.startsWith("+") ? "+" : "") + digits;
        }
        return phone.length() <= 20 ? Optional.of(phone) : Optional.empty();
    }

    public String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
