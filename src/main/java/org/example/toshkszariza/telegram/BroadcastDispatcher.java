package org.example.toshkszariza.telegram;

import org.example.toshkszariza.service.BroadcastDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BroadcastDispatcher {
    private static final Logger log = LoggerFactory.getLogger(BroadcastDispatcher.class);
    private final TelegramApiClient api;

    public BroadcastDispatcher(TelegramApiClient api) {
        this.api = api;
    }

    /** Umumiy xabar polling oqimini band qilmasligi uchun alohida oqimda yuboriladi. */
    @Async("broadcastExecutor")
    public void dispatch(long adminChatId, BroadcastDelivery delivery) {
        int delivered = 0;
        int failed = 0;
        String text = "📣 <b>Administrator xabari</b>\n\n" + BotTexts.escape(delivery.message());

        for (long recipientChatId : delivery.recipientChatIds()) {
            try {
                api.sendMessage(recipientChatId, text, BotKeyboards.mainMenu(false));
                delivered++;
                // Telegram global limitiga keskin urilmaslik uchun xabarlar oralig'i qisqa saqlanadi.
                Thread.sleep(40);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                failed += delivery.recipientChatIds().size() - delivered - failed;
                break;
            } catch (RuntimeException exception) {
                failed++;
                log.warn("Broadcast chat {} ga yuborilmadi: {}", recipientChatId, exception.getMessage());
            }
        }

        try {
            api.sendMessage(
                    adminChatId,
                    "📣 <b>Umumiy xabar yakunlandi.</b>\n\n"
                            + "✅ Yuborildi: " + delivered + "\n"
                            + "⚠️ Yuborilmadi: " + failed,
                    BotKeyboards.mainMenu(true)
            );
        } catch (RuntimeException exception) {
            log.warn("Broadcast natijasi adminga yuborilmadi: {}", exception.getMessage());
        }
    }
}
