package org.example.toshkszariza.telegram;

import org.example.toshkszariza.config.BotProperties;
import org.example.toshkszariza.domain.ApplicationAttachmentType;
import org.example.toshkszariza.telegram.model.TelegramEnvelope;
import org.example.toshkszariza.telegram.model.TelegramDestination;
import org.example.toshkszariza.telegram.model.TelegramMessage;
import org.example.toshkszariza.telegram.model.TelegramUpdate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramApiClient {
    private final RestClient restClient;
    private final BotProperties properties;

    public TelegramApiClient(BotProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.longPollTimeoutSeconds() + 10L));
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + properties.token())
                .requestFactory(requestFactory)
                .build();
    }

    public List<TelegramUpdate> getUpdates(Long offset) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (offset != null) {
            body.put("offset", offset);
        }
        body.put("limit", 100);
        body.put("timeout", properties.longPollTimeoutSeconds());
        body.put("allowed_updates", List.of("message", "callback_query"));
        TelegramEnvelope<List<TelegramUpdate>> response = post(
                "/getUpdates", body, new ParameterizedTypeReference<>() { }
        );
        return response.result() == null ? List.of() : response.result();
    }

    public TelegramMessage sendMessage(long chatId, String text, Object replyMarkup) {
        return sendMessage(new TelegramDestination(chatId, null, null), text, replyMarkup);
    }

    public TelegramMessage sendMessage(TelegramDestination destination, String text, Object replyMarkup) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", destination.chatId());
        addTopic(body, destination);
        body.put("text", text);
        body.put("parse_mode", "HTML");
        body.put("disable_web_page_preview", true);
        if (replyMarkup != null) {
            body.put("reply_markup", replyMarkup);
        }
        TelegramEnvelope<TelegramMessage> response = post(
                "/sendMessage", body, new ParameterizedTypeReference<>() { }
        );
        return response.result();
    }

    /** Telegram file_id orqali media faylni qayta yuklamasdan tez yuboradi. */
    public TelegramMessage sendAttachment(long chatId, ApplicationAttachmentType type, String fileId) {
        return sendAttachment(new TelegramDestination(chatId, null, null), type, fileId);
    }

    public TelegramMessage sendAttachment(
            TelegramDestination destination,
            ApplicationAttachmentType type,
            String fileId
    ) {
        String path;
        String field;
        switch (type) {
            case PHOTO -> {
                path = "/sendPhoto";
                field = "photo";
            }
            case VIDEO -> {
                path = "/sendVideo";
                field = "video";
            }
            case VIDEO_NOTE -> {
                path = "/sendVideoNote";
                field = "video_note";
            }
            default -> throw new IllegalArgumentException("Noma'lum media turi");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", destination.chatId());
        body.put(field, fileId);
        addTopic(body, destination);
        TelegramEnvelope<TelegramMessage> response = post(path, body, new ParameterizedTypeReference<>() { });
        return response.result();
    }

    private void addTopic(Map<String, Object> body, TelegramDestination destination) {
        if (destination.directMessagesTopicId() != null) {
            body.put("direct_messages_topic_id", destination.directMessagesTopicId());
        } else if (destination.messageThreadId() != null) {
            body.put("message_thread_id", destination.messageThreadId());
        }
    }

    public void editMessageText(long chatId, int messageId, String text, Object replyMarkup) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", chatId);
        body.put("message_id", messageId);
        body.put("text", text);
        body.put("parse_mode", "HTML");
        body.put("disable_web_page_preview", true);
        body.put("reply_markup", replyMarkup == null ? Map.of("inline_keyboard", List.of()) : replyMarkup);
        post("/editMessageText", body, new ParameterizedTypeReference<TelegramEnvelope<Object>>() { });
    }

    public void answerCallback(String callbackId, String text, boolean showAlert) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("callback_query_id", callbackId);
        if (text != null && !text.isBlank()) {
            body.put("text", text);
        }
        body.put("show_alert", showAlert);
        post("/answerCallbackQuery", body, new ParameterizedTypeReference<TelegramEnvelope<Boolean>>() { });
    }

    public void registerCommands() {
        List<Map<String, String>> commands = new ArrayList<>();
        commands.add(Map.of("command", "start", "description", "Bosh menyu"));
        commands.add(Map.of("command", "new", "description", "Yangi ariza"));
        commands.add(Map.of("command", "my", "description", "Arizalarim"));
        commands.add(Map.of("command", "id", "description", "Telegram ID raqamim"));
        commands.add(Map.of("command", "cancel", "description", "Joriy amalni bekor qilish"));
        commands.add(Map.of("command", "help", "description", "Yordam"));
        commands.add(Map.of("command", "pending", "description", "Admin: kutilayotgan arizalar"));
        commands.add(Map.of("command", "broadcast", "description", "Admin: barcha userlarga xabar"));
        commands.add(Map.of("command", "week", "description", "Admin: 7 kunlik hisobot"));
        commands.add(Map.of("command", "bind", "description", "Bosh admin: guruh/kanalni bog'lash"));
        commands.add(Map.of("command", "admins", "description", "Adminlar ro'yxati"));
        commands.add(Map.of("command", "addadmin", "description", "Bosh admin: admin qo'shish"));
        post(
                "/setMyCommands",
                Map.of("commands", commands),
                new ParameterizedTypeReference<TelegramEnvelope<Boolean>>() { }
        );
    }

    private <T> TelegramEnvelope<T> post(
            String path,
            Object body,
            ParameterizedTypeReference<TelegramEnvelope<T>> responseType
    ) {
        TelegramEnvelope<T> response = restClient.post()
                .uri(path)
                .body(body)
                .retrieve()
                .body(responseType);
        if (response == null || !response.ok()) {
            String description = response == null ? "Telegram javob qaytarmadi" : response.description();
            throw new TelegramApiException(description);
        }
        return response;
    }
}
