package org.example.toshkszariza.telegram;

import org.example.toshkszariza.config.BotProperties;
import org.example.toshkszariza.service.ApplicationStatisticsService;
import org.example.toshkszariza.service.ReviewChatService;
import org.example.toshkszariza.telegram.model.TelegramDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** Har dushanba soat 09:00 da bog'langan guruh/kanalga 7 kunlik hisobot yuboradi. */
@Component
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WeeklyReportScheduler {
    private static final Logger log = LoggerFactory.getLogger(WeeklyReportScheduler.class);

    private final ApplicationStatisticsService statisticsService;
    private final ReviewChatService reviewChatService;
    private final TelegramApiClient api;
    private final BotProperties properties;

    public WeeklyReportScheduler(
            ApplicationStatisticsService statisticsService,
            ReviewChatService reviewChatService,
            TelegramApiClient api,
            BotProperties properties
    ) {
        this.statisticsService = statisticsService;
        this.reviewChatService = reviewChatService;
        this.api = api;
        this.properties = properties;
    }

    @Scheduled(cron = "${telegram.bot.weekly-report-cron:0 0 9 * * MON}", zone = "Asia/Tashkent")
    public void sendWeeklyReport() {
        if (!properties.isConfigured()) {
            return;
        }
        reviewChatService.configuredChat().ifPresent(chat -> {
            try {
                api.sendMessage(
                        new TelegramDestination(
                                chat.chatId(),
                                chat.messageThreadId(),
                                chat.directMessagesTopicId()
                        ),
                        BotTexts.weeklyStatistics(statisticsService.calculate()),
                        null
                );
            } catch (RuntimeException exception) {
                log.warn("Haftalik hisobot yuborilmadi: {}", exception.getMessage());
            }
        });
    }
}
