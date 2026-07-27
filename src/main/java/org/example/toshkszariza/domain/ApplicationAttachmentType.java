package org.example.toshkszariza.domain;

/** Arizaga Telegram orqali biriktiriladigan media turi. */
public enum ApplicationAttachmentType {
    PHOTO("Rasm", "Ariza rasm orqali yuborildi."),
    VIDEO("Video", "Ariza video orqali yuborildi."),
    VIDEO_NOTE("Video-xabar", "Ariza video-xabar orqali yuborildi.");

    private final String label;
    private final String defaultDescription;

    ApplicationAttachmentType(String label, String defaultDescription) {
        this.label = label;
        this.defaultDescription = defaultDescription;
    }

    public String label() { return label; }
    public String defaultDescription() { return defaultDescription; }
}
