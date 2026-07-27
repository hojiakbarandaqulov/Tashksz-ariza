package org.example.toshkszariza.telegram;

import org.example.toshkszariza.domain.ApplicationCategory;
import org.example.toshkszariza.domain.ApplicationRegion;
import org.example.toshkszariza.service.DraftField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Telegram keyboard JSON tuzilmalari shu fabrikada markazlashtirilgan (Factory pattern). */
public final class BotKeyboards {
    private BotKeyboards() {
    }

    public static Map<String, Object> mainMenu(boolean admin) {
        return mainMenu(admin, false);
    }

    public static Map<String, Object> mainMenu(boolean admin, boolean superAdmin) {
        List<List<Map<String, Object>>> rows = new ArrayList<>();
        rows.add(List.of(button("📥 Yangi ariza"), button("📋 Arizalarim")));
        if (admin) {
            rows.add(List.of(button("🗂 Kutilayotgan arizalar"), button("📣 Xabar yuborish")));
            rows.add(List.of(button("📊 7 kunlik hisobot")));
        }
        if (superAdmin) {
            rows.add(List.of(button("👥 Adminlar"), button("➕ Admin qo'shish")));
        }
        return Map.of("keyboard", rows, "resize_keyboard", true);
    }

    public static Map<String, Object> phone() {
        return Map.of(
                "keyboard", List.of(
                        List.of(Map.of("text", "📱 Telefon raqamni yuborish", "request_contact", true)),
                        List.of(button("❌ Bekor qilish"))
                ),
                "resize_keyboard", true,
                "one_time_keyboard", true
        );
    }

    public static Map<String, Object> categories() {
        return Map.of(
                "keyboard", List.of(
                        List.of(button(ApplicationCategory.TECHNICAL_TERMS.label()), button(ApplicationCategory.EMERGENCY.label())),
                        List.of(button(ApplicationCategory.METER.label()), button(ApplicationCategory.CONNECTION.label())),
                        List.of(button(ApplicationCategory.OTHER.label())),
                        List.of(button("❌ Bekor qilish"))
                ),
                "resize_keyboard", true,
                "one_time_keyboard", true
        );
    }

    /** Faqat buyurtmachi belgilagan 9 ta KSZ hududi chiqariladi. */
    public static Map<String, Object> regions() {
        return Map.of(
                "keyboard", List.of(
                        List.of(button(ApplicationRegion.BEKTEMIR_KSZ.label()), button(ApplicationRegion.SERGELI_KSZ.label())),
                        List.of(button(ApplicationRegion.MIRZO_ULUGBEK_KSZ.label())),
                        List.of(button(ApplicationRegion.MIRZO_ULUGBEK_KSZ_2_CHINGILDI.label())),
                        List.of(button(ApplicationRegion.CHILONZOR_KSZ.label()), button(ApplicationRegion.UCHTEPA_KSZ.label())),
                        List.of(button(ApplicationRegion.CHILONZOR_2_SOFTPLAST_KSZ.label())),
                        List.of(button(ApplicationRegion.YAKKASAROY_KSZ.label()), button(ApplicationRegion.YUNUSOBOD_KSZ.label())),
                        List.of(button("❌ Bekor qilish"))
                ),
                "resize_keyboard", true,
                "one_time_keyboard", true
        );
    }

    public static Map<String, Object> removeReplyKeyboard() {
        return Map.of("remove_keyboard", true);
    }

    public static Map<String, Object> draftConfirmation() {
        return Map.of("inline_keyboard", List.of(
                List.of(inline("✅ Yuborish", "draft:submit")),
                List.of(
                        inline("🗺 Hudud", edit(DraftField.REGION)),
                        inline("🏢 Korxona va tavsif", edit(DraftField.APPLICATION_DETAILS))
                ),
                List.of(inline("❌ Bekor qilish", "draft:cancel"))
        ));
    }

    public static Map<String, Object> adminActions(long applicationId) {
        return Map.of("inline_keyboard", List.of(List.of(
                inline("✅ Qabul qilish", "admin:accept:" + applicationId),
                inline("↩️ Qaytarish", "admin:reject:" + applicationId)
        )));
    }

    public static Map<String, Object> correction(long applicationId) {
        return Map.of("inline_keyboard", List.of(List.of(
                inline("✏️ Tuzatish", "application:correct:" + applicationId)
        )));
    }

    public static Map<String, Object> cancelRejection() {
        return Map.of("inline_keyboard", List.of(List.of(
                inline("❌ Rad etishni bekor qilish", "admin:reject-cancel")
        )));
    }

    public static Map<String, Object> broadcastConfirmation(int recipientCount) {
        return Map.of("inline_keyboard", List.of(
                List.of(inline("📣 " + recipientCount + " ta userga yuborish", "broadcast:send")),
                List.of(inline("❌ Bekor qilish", "broadcast:cancel"))
        ));
    }

    private static String edit(DraftField field) {
        return "draft:edit:" + field.callbackKey();
    }

    private static Map<String, Object> button(String text) {
        return Map.of("text", text);
    }

    private static Map<String, Object> inline(String text, String callbackData) {
        return Map.of("text", text, "callback_data", callbackData);
    }
}
