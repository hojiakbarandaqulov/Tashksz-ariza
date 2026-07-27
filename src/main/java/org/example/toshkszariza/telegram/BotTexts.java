package org.example.toshkszariza.telegram;

import org.example.toshkszariza.domain.ApplicationStatus;
import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.service.ApplicationView;
import org.example.toshkszariza.service.DraftView;
import org.example.toshkszariza.service.WeeklyStatistics;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class BotTexts {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Tashkent"));

    private BotTexts() {
    }

    public static String welcome(String firstName, boolean admin) {
        String adminText = admin ? "\n\nSiz administrator sifatida kirdingiz. /pending — navbatdagi arizalar." : "";
        return "Assalomu alaykum, <b>" + escape(firstName) + "</b>!\n\n"
                + "Bu bot orqali TOSHKSZga ariza yuborishingiz va uning holatini kuzatishingiz mumkin."
                + adminText;
    }

    public static String help(boolean admin, boolean superAdmin) {
        String adminCommand = admin
                ? "\n/pending — ko'rib chiqilmagan arizalar\n/broadcast — barcha userlarga xabar"
                        + "\n/week — oxirgi 7 kunlik hisobot"
                : "";
        String superAdminCommand = superAdmin
                ? "\n/addadmin — yangi admin qo'shish\n/admins — adminlar ro'yxati"
                        + "\n/bind — joriy guruh/kanalni arizalarga bog'lash"
                : "";
        return "<b>Buyruqlar</b>\n"
                + "/new — yangi ariza yaratish\n"
                + "/my — oxirgi arizalarim\n"
                + "/id — Telegram ID raqamim\n"
                + "/cancel — joriy amalni bekor qilish\n"
                + "/help — yordam" + adminCommand + superAdminCommand;
    }

    public static String prompt(ConversationStep step) {
        return switch (step) {
            case WAITING_FULL_NAME -> "👤 <b>F.I.O.</b>ni to'liq kiriting:\nMasalan: Aliyev Anvar Akmalovich";
            case WAITING_PHONE -> "📞 <b>Telefon raqamingizni</b> yuboring:\nMasalan: +998 90 123 45 67";
            case WAITING_ORGANIZATION -> "🏢 <b>Korxona nomini</b> kiriting:";
            case WAITING_REGION -> "📍 <b>Hududni tanlang:</b>";
            case WAITING_CATEGORY -> "📌 <b>Kategoriyani</b> tanlang:";
            case WAITING_DESCRIPTION -> "📝 Muammoni batafsil <b>tavsiflang</b> (10–2000 belgi):";
            case WAITING_APPLICATION_DETAILS -> "🏢 <b>Kompaniya nomini kiriting va arizani yozing:</b>\n\n"
                    + "Bitta xabarda 2 qator qilib yuboring:\n"
                    + "<code>Asia polymer system mchj\n6 blok yonida transformatorda nosozlik</code>\n\n"
                    + "📎 Arizani <b>rasm, video yoki video-xabar</b> shaklida ham yuborishingiz mumkin. "
                    + "Rasm/video izohiga kompaniya nomini yozing.";
            case CONFIRMING -> "Ma'lumotlarni tekshirib, «Yuborish» tugmasini bosing.";
            case IDLE -> "Yangi ariza boshlash uchun pastdagi tugmani bosing.";
        };
    }

    public static String draft(DraftView draft) {
        String heading = draft.editingApplicationId() == null
                ? "📥 <b>Yangi ariza</b>"
                : "✏️ <b>Ariza #" + number(draft.editingApplicationId()) + " — yangi tahrir</b>";
        return heading + ":\n\n"
                + "🗺 <b>Hudud:</b> " + escape(draft.region() == null ? "" : draft.region().label()) + "\n"
                + "🏢 <b>Korxona nomi:</b> " + escape(draft.organizationName()) + "\n"
                + "📝 <b>Tavsif:</b> " + escape(draft.description())
                + attachmentLine(draft.attachmentType()) + "\n\n"
                + "Ma'lumotlarni tekshiring.";
    }

    public static String application(ApplicationView application, boolean includeUser) {
        StringBuilder text = new StringBuilder("📥 <b>Yangi ariza:</b> #")
                .append(number(application.id())).append("\n\n")
                .append("🗺 <b>Hudud:</b> ").append(escape(regionLabel(application))).append("\n")
                .append("🏢 <b>Korxona nomi:</b> ").append(escape(application.organizationName())).append("\n")
                .append("📝 <b>Tavsif:</b> ").append(escape(application.description()))
                .append(attachmentLine(application.attachmentType())).append("\n\n")
                .append(statusIcon(application.status())).append(" <b>Holat:</b> ")
                .append(escape(application.status().label())).append("\n")
                .append("🔁 <b>Tahrir:</b> ").append(application.revision()).append("\n")
                .append("🕒 <b>Yuborildi:</b> ").append(DATE_FORMAT.format(application.submittedAt()));
        if (includeUser && application.telegramUsername() != null && !application.telegramUsername().isBlank()) {
            text.append("\n💬 <b>Telegram:</b> @").append(escape(application.telegramUsername()));
        }
        if (application.rejectionReason() != null) {
            text.append("\n\n↩️ <b>Qaytarish sababi:</b> ").append(escape(application.rejectionReason()));
        }
        return text.toString();
    }

    public static String userApplicationList(List<ApplicationView> applications) {
        if (applications.isEmpty()) {
            return "Sizda hali yuborilgan arizalar yo'q.";
        }
        StringBuilder text = new StringBuilder("📋 <b>Oxirgi arizalaringiz</b>\n");
        for (ApplicationView application : applications) {
            text.append("\n").append(statusIcon(application.status()))
                    .append(" <b>#").append(number(application.id())).append("</b> — ")
                    .append(escape(regionLabel(application))).append("\n")
                    .append("Korxona: ").append(escape(application.organizationName())).append("\n")
                    .append("Holat: ").append(escape(application.status().label()));
            if (application.rejectionReason() != null) {
                text.append("\nSabab: ").append(escape(application.rejectionReason()));
            }
            text.append("\n");
        }
        return text.toString();
    }

    public static String acceptedNotice(ApplicationView application) {
        return "✅ <b>Arizangiz qabul qilindi!</b>\n\nAriza raqami: #" + number(application.id())
                + "\nHudud: " + escape(regionLabel(application));
    }

    public static String rejectedNotice(ApplicationView application) {
        return "↩️ <b>Arizangiz tuzatish uchun qaytarildi.</b>\n\nAriza raqami: #" + number(application.id())
                + "\n<b>Sabab:</b> " + escape(application.rejectionReason())
                + "\n\nQuyidagi tugma orqali ma'lumotlarni to'g'rilab, qayta yuboring.";
    }

    public static String weeklyStatistics(WeeklyStatistics statistics) {
        return "📊 <b>Oxirgi 7 kunlik arizalar hisoboti</b>\n\n"
                + "📥 Jami kelgan: <b>" + statistics.total() + "</b>\n"
                + "⏳ Kutilmoqda: <b>" + statistics.pending() + "</b>\n"
                + "✅ Qabul qilingan: <b>" + statistics.accepted() + "</b>\n"
                + "↩️ Qaytarilgan: <b>" + statistics.rejected() + "</b>";
    }

    private static String statusIcon(ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "⏳";
            case ACCEPTED -> "✅";
            case REJECTED -> "↩️";
        };
    }

    private static String regionLabel(ApplicationView application) {
        return application.region() == null ? "Ko'rsatilmagan (eski ariza)" : application.region().label();
    }

    private static String number(long id) {
        return String.format("%06d", id);
    }

    private static String attachmentLine(org.example.toshkszariza.domain.ApplicationAttachmentType type) {
        return type == null ? "" : "\n📎 <b>Ilova:</b> " + escape(type.label());
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
