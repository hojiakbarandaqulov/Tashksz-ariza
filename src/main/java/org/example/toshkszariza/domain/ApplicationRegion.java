package org.example.toshkszariza.domain;

import java.util.Arrays;
import java.util.Optional;

/** Foydalanuvchi tanlaydigan KSZ hududlari; eski qiymatlar tarixiy arizalar uchun saqlanadi. */
public enum ApplicationRegion {
    BEKTEMIR_KSZ("Bektemir KSZ", true),
    MIRZO_ULUGBEK_KSZ("Mirzo Ulug'bek KSZ", true),
    MIRZO_ULUGBEK_KSZ_2_CHINGILDI("Mirzo Ulug'bek KSZ 2 (Chingildi)", true),
    CHILONZOR_KSZ("Chilonzor KSZ", true),
    CHILONZOR_2_SOFTPLAST_KSZ("Chulonzor 2 SoftPlast KSZ", true),
    SERGELI_KSZ("Sergeli KSZ", true),
    UCHTEPA_KSZ("Uchtepa KSZ", true),
    YAKKASAROY_KSZ("Yakkasaroy KSZ", true),
    YUNUSOBOD_KSZ("Yunusobod KSZ", true),

    // Avval tanlangan tumanlar bazadagi eski arizalarni ochish uchun saqlanadi.
    BEKTEMIR_DISTRICT("Bektemir tumani", false),
    CHILONZOR_DISTRICT("Chilonzor tumani", false),
    YASHNOBOD_DISTRICT("Yashnobod tumani", false),
    MIROBOD_DISTRICT("Mirobod tumani", false),
    MIRZO_ULUGBEK_DISTRICT("Mirzo Ulug'bek tumani", false),
    OLMAZOR_DISTRICT("Olmazor tumani", false),
    SERGELI_DISTRICT("Sergeli tumani", false),
    SHAYXONTOHUR_DISTRICT("Shayxontohur tumani", false),
    UCHTEPA_DISTRICT("Uchtepa tumani", false),
    YAKKASAROY_DISTRICT("Yakkasaroy tumani", false),
    YANGIHAYOT_DISTRICT("Yangihayot tumani", false),
    YUNUSOBOD_DISTRICT("Yunusobod tumani", false),

    // Ushbu qiymatlar avvalgi arizalarni xatosiz o'qish uchun qoldirilgan.
    ANDIJAN("Andijon", false),
    BUKHARA("Buxoro", false),
    JIZZAKH("Jizzax", false),
    KASHKADARYA("Qashqadaryo", false),
    NAVOI("Navoiy", false),
    NAMANGAN("Namangan", false),
    SAMARKAND("Samarqand", false),
    SURKHANDARYA("Surxondaryo", false),
    SYRDARYA("Sirdaryo", false),
    TASHKENT("Toshkent", false),
    FERGANA("Farg'ona", false),
    KHOREZM("Xorazm", false);

    private final String label;
    private final boolean selectable;

    ApplicationRegion(String label, boolean selectable) {
        this.label = label;
        this.selectable = selectable;
    }

    public String label() {
        return label;
    }

    public static Optional<ApplicationRegion> fromLabel(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(ApplicationRegion::isSelectable)
                .filter(region -> region.label.equalsIgnoreCase(value.trim()))
                .findFirst();
    }

    public boolean isSelectable() {
        return selectable;
    }
}
