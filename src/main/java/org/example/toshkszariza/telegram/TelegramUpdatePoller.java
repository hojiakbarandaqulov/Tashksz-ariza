package org.example.toshkszariza.telegram;

import org.example.toshkszariza.config.BotProperties;
import org.example.toshkszariza.telegram.model.TelegramUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/** Long polling alohida scheduler oqimida ishlaydi; HTTP so'rovlar web oqimini band qilmaydi. */
@Component
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TelegramUpdatePoller {
    private static final Logger log = LoggerFactory.getLogger(TelegramUpdatePoller.class);

    private final TelegramApiClient api;
    private final TelegramUpdateHandler handler;
    private final BotProperties properties;
    private Long nextOffset;
    private boolean initialized;
    private boolean configurationWarningLogged;

    public TelegramUpdatePoller(TelegramApiClient api, TelegramUpdateHandler handler, BotProperties properties) {
        this.api = api;
        this.handler = handler;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${telegram.bot.poll-delay-ms:300}")
    public void poll() {
        if (!properties.isConfigured()) {
            if (!configurationWarningLogged) {
                log.warn("Bot ishga tushmadi: TELEGRAM_BOT_TOKEN ga yangi tokenni kiriting");
                configurationWarningLogged = true;
            }
            return;
        }
        try {
            initializeOnce();
            List<TelegramUpdate> updates = api.getUpdates(nextOffset);
            for (TelegramUpdate update : updates) {
                try {
                    handler.handle(update);
                } catch (RuntimeException exception) {
                    // Bitta xato update keyingi foydalanuvchilarning xabarlarini to'xtatib qo'ymasligi kerak.
                    log.error("Telegram update {} bajarilmadi", update.updateId(), exception);
                } finally {
                    nextOffset = update.updateId() + 1;
                }
            }
        } catch (RuntimeException exception) {
            log.warn("Telegram bilan aloqa vaqtincha uzildi: {}", exception.getMessage());
        }
    }

    private void initializeOnce() {
        if (!initialized) {
            api.registerCommands();
            initialized = true;
            log.info("@{} bot ishga tushdi", properties.username());
        }
    }
}
