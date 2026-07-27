package org.example.toshkszariza.domain;

public enum ApplicationStatus {
    PENDING("Ko'rib chiqilmoqda"),
    ACCEPTED("Qabul qilindi"),
    REJECTED("Tuzatish uchun qaytarildi");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
