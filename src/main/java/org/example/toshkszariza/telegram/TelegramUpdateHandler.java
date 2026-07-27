package org.example.toshkszariza.telegram;

import org.example.toshkszariza.domain.ApplicationStatus;
import org.example.toshkszariza.domain.ApplicationAttachmentType;
import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.service.ApplicationService;
import org.example.toshkszariza.service.ApplicationView;
import org.example.toshkszariza.service.AdminManagementService;
import org.example.toshkszariza.service.AdminService;
import org.example.toshkszariza.service.BotBusinessException;
import org.example.toshkszariza.service.BroadcastService;
import org.example.toshkszariza.service.ConversationService;
import org.example.toshkszariza.service.DraftField;
import org.example.toshkszariza.service.FlowOutcome;
import org.example.toshkszariza.service.RejectionResult;
import org.example.toshkszariza.service.ReviewChatService;
import org.example.toshkszariza.service.ApplicationStatisticsService;
import org.example.toshkszariza.telegram.model.TelegramCallbackQuery;
import org.example.toshkszariza.telegram.model.TelegramContact;
import org.example.toshkszariza.telegram.model.TelegramMessage;
import org.example.toshkszariza.telegram.model.TelegramUpdate;
import org.example.toshkszariza.telegram.model.TelegramDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class TelegramUpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(TelegramUpdateHandler.class);

    private final TelegramApiClient api;
    private final ConversationService conversationService;
    private final ApplicationService applicationService;
    private final AdminService adminService;
    private final AdminManagementService adminManagementService;
    private final BroadcastService broadcastService;
    private final BroadcastDispatcher broadcastDispatcher;
    private final ReviewChatService reviewChatService;
    private final ApplicationStatisticsService statisticsService;

    public TelegramUpdateHandler(
            TelegramApiClient api,
            ConversationService conversationService,
            ApplicationService applicationService,
            AdminService adminService,
            AdminManagementService adminManagementService,
            BroadcastService broadcastService,
            BroadcastDispatcher broadcastDispatcher,
            ReviewChatService reviewChatService,
            ApplicationStatisticsService statisticsService
    ) {
        this.api = api;
        this.conversationService = conversationService;
        this.applicationService = applicationService;
        this.adminService = adminService;
        this.adminManagementService = adminManagementService;
        this.broadcastService = broadcastService;
        this.broadcastDispatcher = broadcastDispatcher;
        this.reviewChatService = reviewChatService;
        this.statisticsService = statisticsService;
    }

    public void handle(TelegramUpdate update) {
        if (update.callbackQuery() != null) {
            handleCallback(update.callbackQuery());
        } else if (update.message() != null) {
            handleMessage(update.message());
        }
    }

    private void handleMessage(TelegramMessage message) {
        if (message.chat() == null || message.from() == null || !message.chat().supportsUserMessages()) {
            return;
        }
        long chatId = message.chat().id();
        long userId = message.from().id();
        TelegramDestination destination = TelegramDestination.from(message);
        conversationService.registerUser(
                chatId,
                userId,
                destination.messageThreadId(),
                destination.directMessagesTopicId(),
                message.chat().isPrivate()
        );
        boolean admin = applicationService.isAdmin(userId);
        String rawText = message.text() != null ? message.text() : message.caption();
        String text = rawText == null ? "" : rawText.trim();

        String command = commandOf(text);
        if (command != null) {
            boolean adminCreated = false;
            if ("start".equals(command)) {
                if (message.chat().isPrivate()) {
                    adminCreated = adminService.registerFirstAdminIfNeeded(
                            userId, chatId, message.from().username()
                    );
                }
                admin = applicationService.isAdmin(userId);
            }
            handleCommand(command, message, admin, adminCreated);
            return;
        }

        if ("📥 Yangi ariza".equals(text)) {
            startNew(destination, userId);
            return;
        }
        if ("📋 Arizalarim".equals(text)) {
            showUserApplications(destination, userId, admin);
            return;
        }
        if ("🗂 Kutilayotgan arizalar".equals(text)) {
            showPending(destination, userId);
            return;
        }
        if ("📣 Xabar yuborish".equals(text)) {
            beginBroadcast(destination, userId);
            return;
        }
        if ("📊 7 kunlik hisobot".equals(text)) {
            showWeeklyStatistics(destination, userId);
            return;
        }
        if ("➕ Admin qo'shish".equals(text)) {
            beginAdminAdd(destination, userId);
            return;
        }
        if ("👥 Adminlar".equals(text)) {
            showAdmins(destination, userId);
            return;
        }
        if ("❌ Bekor qilish".equals(text)) {
            cancelAll(destination, userId, admin);
            return;
        }

        if (adminManagementService.isWaiting(userId)) {
            handleAdminId(destination, userId, text);
            return;
        }
        if (admin && broadcastService.isWaitingForText(userId)) {
            handleBroadcastText(destination, userId, text);
            return;
        }
        if (admin && broadcastService.isActive(userId)) {
            api.sendMessage(destination, "Xabar tayyor. Preview ostidagi yuborish yoki bekor qilish tugmasini bosing.", null);
            return;
        }

        // Admin qaytarish tugmasini bosgach, uning keyingi matni rad etish sababi hisoblanadi.
        if (admin && applicationService.isWaitingForReason(userId)) {
            handleRejectionReason(destination, userId, text);
            return;
        }

        ConversationStep step = conversationService.currentStep(userId);
        if (step == ConversationStep.IDLE) {
            // Guruhdagi oddiy suhbatlarga bot aralashmaydi; ariza faqat /new bilan boshlanadi.
            if (message.chat().isPrivate()) {
                api.sendMessage(destination, BotTexts.prompt(step), mainMenu(userId));
            }
            return;
        }
        if (step == ConversationStep.CONFIRMING) {
            MediaAttachment media = mediaFrom(message);
            if (media != null) {
                var draft = conversationService.attachMedia(chatId, userId, media.type(), media.fileId());
                api.sendMessage(
                        destination,
                        "✅ Media arizaga biriktirildi.\n\n" + BotTexts.draft(draft),
                        BotKeyboards.draftConfirmation()
                );
                return;
            }
            api.sendMessage(
                    destination,
                    BotTexts.draft(conversationService.draft(chatId, userId)),
                    BotKeyboards.draftConfirmation()
            );
            return;
        }

        TelegramContact contact = message.contact();
        if (contact != null && contact.userId() != null && contact.userId() != userId) {
            api.sendMessage(destination, "O'zingizga tegishli telefon raqamni yuboring.", BotKeyboards.phone());
            return;
        }
        String contactPhone = contact == null ? null : contact.phoneNumber();
        MediaAttachment media = mediaFrom(message);
        FlowOutcome outcome = conversationService.handleInput(
                chatId,
                userId,
                text,
                contactPhone,
                media == null ? null : media.type(),
                media == null ? null : media.fileId()
        );
        if (!outcome.result().accepted()) {
            api.sendMessage(
                    destination,
                    "⚠️ " + BotTexts.escape(outcome.result().errorMessage()),
                    keyboardFor(outcome.result().currentStep(), admin)
            );
            return;
        }

        if (outcome.result().currentStep() == ConversationStep.CONFIRMING) {
            api.sendMessage(destination, BotTexts.draft(outcome.draft()), BotKeyboards.draftConfirmation());
        } else {
            api.sendMessage(
                    destination,
                    BotTexts.prompt(outcome.result().currentStep()),
                    keyboardFor(outcome.result().currentStep(), admin)
            );
        }
    }

    private void handleCommand(String command, TelegramMessage message, boolean admin, boolean adminCreated) {
        long chatId = message.chat().id();
        long userId = message.from().id();
        TelegramDestination destination = TelegramDestination.from(message);
        switch (command) {
            case "start" -> {
                String createdNotice = adminCreated
                        ? "👑 <b>Siz bosh administrator sifatida avtomatik yaratildingiz.</b>\n"
                                + "Endi boshqa adminlarni ham qo'sha olasiz.\n\n"
                        : "";
                api.sendMessage(
                        destination,
                        createdNotice + BotTexts.welcome(message.from().firstName(), admin),
                        mainMenu(userId)
                );
            }
            case "new" -> startNew(destination, userId);
            case "my" -> showUserApplications(destination, userId, admin);
            case "pending" -> showPending(destination, userId);
            case "broadcast" -> beginBroadcast(destination, userId);
            case "addadmin" -> beginAdminAdd(destination, userId);
            case "admins" -> showAdmins(destination, userId);
            case "week" -> showWeeklyStatistics(destination, userId);
            case "bind" -> bindReviewChat(message, destination, userId);
            case "id" -> api.sendMessage(
                    destination,
                    "🆔 Sizning Telegram ID raqamingiz: <code>" + userId + "</code>",
                    mainMenu(userId)
            );
            case "cancel" -> cancelAll(destination, userId, admin);
            case "help" -> api.sendMessage(
                    destination,
                    BotTexts.help(admin, adminService.isSuperAdmin(userId)),
                    mainMenu(userId)
            );
            default -> api.sendMessage(destination, "Noma'lum buyruq. /help ni bosing.", mainMenu(userId));
        }
    }

    private void startNew(TelegramDestination destination, long userId) {
        if (applicationService.isAdmin(userId)) {
            applicationService.cancelRejection(userId);
            broadcastService.cancel(userId);
            adminManagementService.cancel(userId);
        }
        ConversationStep firstStep = conversationService.startNew(destination.chatId(), userId);
        api.sendMessage(destination, BotTexts.prompt(firstStep), keyboardFor(firstStep, applicationService.isAdmin(userId)));
    }

    private void cancelAll(TelegramDestination destination, long userId, boolean admin) {
        conversationService.cancel(destination.chatId(), userId);
        if (admin) {
            applicationService.cancelRejection(userId);
            broadcastService.cancel(userId);
            adminManagementService.cancel(userId);
        }
        api.sendMessage(destination, "❌ Joriy amal bekor qilindi.", mainMenu(userId));
    }

    private void showUserApplications(TelegramDestination destination, long userId, boolean admin) {
        List<ApplicationView> applications = applicationService.userApplications(userId);
        api.sendMessage(destination, BotTexts.userApplicationList(applications), mainMenu(userId));
        applications.stream()
                .filter(application -> application.status() == ApplicationStatus.REJECTED)
                .forEach(application -> api.sendMessage(
                        destination,
                        "✏️ #" + String.format("%06d", application.id()) + " arizani tuzatish mumkin.",
                        BotKeyboards.correction(application.id())
                ));
    }

    private void showPending(TelegramDestination destination, long userId) {
        if (!applicationService.isAdmin(userId)) {
            api.sendMessage(destination, "Bu bo'lim faqat administrator uchun.", BotKeyboards.mainMenu(false));
            return;
        }
        List<ApplicationView> pending = applicationService.pendingApplications(userId);
        long count = applicationService.pendingCount(userId);
        api.sendMessage(destination, "🗂 Ko'rib chiqilmagan arizalar: <b>" + count + "</b>", null);
        if (pending.isEmpty()) {
            api.sendMessage(destination, "Hozir navbatda ariza yo'q.", mainMenu(userId));
            return;
        }
        pending.forEach(application -> sendApplicationToAdmin(destination, application, false));
    }

    private void handleCallback(TelegramCallbackQuery callback) {
        if (callback.from() == null || callback.message() == null || callback.message().chat() == null) {
            return;
        }
        try {
            routeCallback(callback);
        } catch (BotBusinessException exception) {
            answerSafely(callback.id(), exception.getMessage(), true);
        } catch (RuntimeException exception) {
            log.error("Callback bajarilmadi", exception);
            answerSafely(callback.id(), "Amalni bajarib bo'lmadi. Qayta urinib ko'ring.", true);
        }
    }

    private void routeCallback(TelegramCallbackQuery callback) {
        String data = callback.data() == null ? "" : callback.data();
        long userId = callback.from().id();
        long chatId = callback.message().chat().id();
        int messageId = callback.message().messageId();
        TelegramDestination callbackDestination = TelegramDestination.from(callback.message());

        if ("draft:submit".equals(data)) {
            ApplicationView application = conversationService.submit(chatId, userId, callback.from().username());
            answerSafely(callback.id(), "Ariza yuborildi", false);
            editSafely(chatId, messageId, BotTexts.application(application, false), null);
            sendSafely(callbackDestination, "✅ Ariza administratorga yuborildi.", mainMenu(userId));
            notifyAdmin(application);
            return;
        }
        if (data.startsWith("draft:edit:")) {
            DraftField field = DraftField.fromCallbackKey(data.substring("draft:edit:".length()));
            conversationService.chooseField(chatId, userId, field);
            answerSafely(callback.id(), "Maydonni qayta kiriting", false);
            sendSafely(callbackDestination, BotTexts.prompt(field.step()), keyboardFor(field.step(), applicationService.isAdmin(userId)));
            return;
        }
        if ("draft:cancel".equals(data)) {
            conversationService.cancel(chatId, userId);
            answerSafely(callback.id(), "Bekor qilindi", false);
            editSafely(chatId, messageId, "❌ Ariza yaratish bekor qilindi.", null);
            sendSafely(callbackDestination, "Bosh menyu", mainMenu(userId));
            return;
        }
        if (data.startsWith("application:correct:")) {
            long applicationId = parseId(data, "application:correct:");
            var draft = conversationService.loadCorrection(chatId, userId, applicationId);
            answerSafely(callback.id(), "Ariza tahrirlashga ochildi", false);
            sendSafely(callbackDestination, BotTexts.draft(draft), BotKeyboards.draftConfirmation());
            return;
        }
        if (data.startsWith("admin:accept:")) {
            ensureAdmin(userId);
            long applicationId = parseId(data, "admin:accept:");
            ApplicationView application = applicationService.accept(userId, applicationId);
            answerSafely(callback.id(), "Ariza qabul qilindi", false);
            editSafely(chatId, messageId, BotTexts.application(application, true), null);
            sendSafely(ownerDestination(application), BotTexts.acceptedNotice(application), BotKeyboards.mainMenu(false));
            return;
        }
        if (data.startsWith("admin:reject:")) {
            ensureAdmin(userId);
            long applicationId = parseId(data, "admin:reject:");
            applicationService.beginRejection(userId, applicationId, chatId, messageId);
            answerSafely(callback.id(), "Rad etish sababini yozing", false);
            sendSafely(
                    callbackDestination,
                    "↩️ Ariza #" + String.format("%06d", applicationId)
                            + " nima sababdan qaytarilayotganini yozing:",
                    BotKeyboards.cancelRejection()
            );
            return;
        }
        if ("admin:reject-cancel".equals(data)) {
            ensureAdmin(userId);
            applicationService.cancelRejection(userId);
            answerSafely(callback.id(), "Rad etish bekor qilindi", false);
            editSafely(chatId, messageId, "Rad etish amali bekor qilindi.", null);
            return;
        }
        if ("broadcast:send".equals(data)) {
            ensureAdmin(userId);
            var delivery = broadcastService.confirm(userId);
            answerSafely(callback.id(), "Umumiy xabar yuborish boshlandi", false);
            editSafely(
                    chatId,
                    messageId,
                    "📣 Xabar <b>" + delivery.recipientChatIds().size() + "</b> ta foydalanuvchiga yuborilmoqda...",
                    null
            );
            broadcastDispatcher.dispatch(chatId, delivery);
            return;
        }
        if ("broadcast:cancel".equals(data)) {
            ensureAdmin(userId);
            broadcastService.cancel(userId);
            answerSafely(callback.id(), "Umumiy xabar bekor qilindi", false);
            editSafely(chatId, messageId, "❌ Umumiy xabar bekor qilindi.", null);
            return;
        }
        throw new BotBusinessException("Noma'lum amal.");
    }

    private void beginBroadcast(TelegramDestination destination, long adminId) {
        if (!applicationService.isAdmin(adminId)) {
            api.sendMessage(destination, "Bu amal faqat administrator uchun.", BotKeyboards.mainMenu(false));
            return;
        }
        applicationService.cancelRejection(adminId);
        adminManagementService.cancel(adminId);
        conversationService.cancel(destination.chatId(), adminId);
        broadcastService.begin(adminId);
        api.sendMessage(
                destination,
                "📣 <b>Barcha foydalanuvchilarga yuboriladigan xabarni yozing:</b>\n\n"
                        + "Yuborishdan oldin sizga preview ko'rsatiladi.",
                BotKeyboards.removeReplyKeyboard()
        );
    }

    private void handleBroadcastText(TelegramDestination destination, long adminId, String text) {
        try {
            var preview = broadcastService.prepare(adminId, text);
            api.sendMessage(
                    destination,
                    "📣 <b>Xabar previewi</b>\n\n" + BotTexts.escape(preview.message())
                            + "\n\n👥 Qabul qiluvchilar: <b>" + preview.recipientCount() + "</b>",
                    BotKeyboards.broadcastConfirmation(preview.recipientCount())
            );
        } catch (BotBusinessException exception) {
            api.sendMessage(destination, "⚠️ " + BotTexts.escape(exception.getMessage()), null);
        }
    }

    private void handleRejectionReason(TelegramDestination destination, long adminId, String reason) {
        try {
            RejectionResult result = applicationService.rejectWithReason(adminId, reason);
            editSafely(
                    result.notificationChatId(),
                    result.notificationMessageId(),
                    BotTexts.application(result.application(), true),
                    null
            );
            sendSafely(
                    ownerDestination(result.application()),
                    BotTexts.rejectedNotice(result.application()),
                    BotKeyboards.correction(result.application().id())
            );
            api.sendMessage(destination, "↩️ Ariza foydalanuvchiga tuzatish uchun qaytarildi.", mainMenu(adminId));
        } catch (BotBusinessException exception) {
            api.sendMessage(destination, "⚠️ " + BotTexts.escape(exception.getMessage()), BotKeyboards.cancelRejection());
        }
    }

    private void notifyAdmin(ApplicationView application) {
        var reviewChat = reviewChatService.configuredChat();
        if (reviewChat.isPresent()) {
            var chat = reviewChat.get();
            sendApplicationToAdmin(
                    new TelegramDestination(
                            chat.chatId(),
                            chat.messageThreadId(),
                            chat.directMessagesTopicId()
                    ),
                    application,
                    true
            );
            return;
        }
        adminService.adminChatIds().forEach(adminChatId -> sendApplicationToAdmin(
                new TelegramDestination(adminChatId, null, null),
                application,
                true
        ));
    }

    private void sendApplicationToAdmin(
            TelegramDestination adminDestination,
            ApplicationView application,
            boolean isNew
    ) {
        if (application.attachmentType() != null && application.attachmentFileId() != null) {
            sendAttachmentSafely(adminDestination, application.attachmentType(), application.attachmentFileId());
        }
        String prefix = isNew ? "🔔 <b>Yangi ariza keldi</b>\n\n" : "";
        sendSafely(
                adminDestination,
                prefix + BotTexts.application(application, true),
                BotKeyboards.adminActions(application.id())
        );
    }

    private void beginAdminAdd(TelegramDestination destination, long userId) {
        try {
            adminManagementService.begin(userId);
            applicationService.cancelRejection(userId);
            broadcastService.cancel(userId);
            conversationService.cancel(destination.chatId(), userId);
            api.sendMessage(
                    destination,
                    "➕ <b>Yangi adminning Telegram ID raqamini yuboring.</b>\n\n"
                            + "U foydalanuvchi avval botga /start yuborishi va /id orqali raqamini olishi kerak.",
                    BotKeyboards.removeReplyKeyboard()
            );
        } catch (BotBusinessException exception) {
            api.sendMessage(destination, "⚠️ " + BotTexts.escape(exception.getMessage()), mainMenu(userId));
        }
    }

    private void handleAdminId(TelegramDestination destination, long superAdminId, String text) {
        try {
            var added = adminManagementService.complete(superAdminId, text);
            api.sendMessage(
                    destination,
                    "✅ <code>" + added.telegramUserId() + "</code> ID egasi administrator qilindi.",
                    mainMenu(superAdminId)
            );
            sendSafely(
                    added.chatId(),
                    "✅ <b>Siz TOSHKSZ Ariza botiga administrator qilib qo'shildingiz.</b>",
                    BotKeyboards.mainMenu(true, false)
            );
        } catch (BotBusinessException exception) {
            api.sendMessage(destination, "⚠️ " + BotTexts.escape(exception.getMessage()), null);
        }
    }

    private void showAdmins(TelegramDestination destination, long requesterId) {
        try {
            StringBuilder text = new StringBuilder("👥 <b>Administratorlar</b>\n");
            adminService.listAdmins(requesterId).forEach(admin -> text
                    .append("\n")
                    .append(admin.superAdmin() ? "👑 " : "👤 ")
                    .append("<code>").append(admin.telegramUserId()).append("</code>")
                    .append(admin.superAdmin() ? " — bosh admin" : ""));
            api.sendMessage(destination, text.toString(), mainMenu(requesterId));
        } catch (BotBusinessException exception) {
            api.sendMessage(destination, "⚠️ " + BotTexts.escape(exception.getMessage()), mainMenu(requesterId));
        }
    }

    private void bindReviewChat(TelegramMessage message, TelegramDestination destination, long requesterId) {
        try {
            var bound = reviewChatService.bind(
                    requesterId,
                    message.chat(),
                    destination.messageThreadId(),
                    destination.directMessagesTopicId()
            );
            String name = bound.title() == null || bound.title().isBlank()
                    ? String.valueOf(bound.chatId())
                    : BotTexts.escape(bound.title());
            api.sendMessage(
                    destination,
                    "✅ <b>Arizalar guruhi/kanali bog'landi:</b> " + name
                            + "\n\nEndi yangi arizalar shu joyga yuboriladi.",
                    mainMenu(requesterId)
            );
        } catch (BotBusinessException exception) {
            api.sendMessage(destination, "⚠️ " + BotTexts.escape(exception.getMessage()), mainMenu(requesterId));
        }
    }

    private void showWeeklyStatistics(TelegramDestination destination, long requesterId) {
        try {
            api.sendMessage(
                    destination,
                    BotTexts.weeklyStatistics(statisticsService.lastSevenDays(requesterId)),
                    mainMenu(requesterId)
            );
        } catch (BotBusinessException exception) {
            api.sendMessage(destination, "⚠️ " + BotTexts.escape(exception.getMessage()), mainMenu(requesterId));
        }
    }

    private TelegramDestination ownerDestination(ApplicationView application) {
        return new TelegramDestination(
                application.userChatId(),
                application.userMessageThreadId(),
                application.userDirectMessagesTopicId()
        );
    }

    private Object mainMenu(long userId) {
        return BotKeyboards.mainMenu(
                applicationService.isAdmin(userId),
                adminService.isSuperAdmin(userId)
        );
    }

    private Object keyboardFor(ConversationStep step, boolean admin) {
        return switch (step) {
            case WAITING_PHONE -> BotKeyboards.phone();
            case WAITING_REGION -> BotKeyboards.regions();
            case WAITING_CATEGORY -> BotKeyboards.categories();
            case IDLE -> BotKeyboards.mainMenu(admin);
            case CONFIRMING -> BotKeyboards.draftConfirmation();
            default -> BotKeyboards.removeReplyKeyboard();
        };
    }

    private String commandOf(String text) {
        if (!text.startsWith("/")) {
            return null;
        }
        String firstPart = text.split("\\s+", 2)[0].substring(1);
        int mentionIndex = firstPart.indexOf('@');
        if (mentionIndex >= 0) {
            firstPart = firstPart.substring(0, mentionIndex);
        }
        return firstPart.toLowerCase(Locale.ROOT);
    }

    private long parseId(String data, String prefix) {
        try {
            return Long.parseLong(data.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            throw new BotBusinessException("Ariza raqami noto'g'ri.");
        }
    }

    private void ensureAdmin(long userId) {
        if (!applicationService.isAdmin(userId)) {
            throw new BotBusinessException("Bu amal faqat administrator uchun.");
        }
    }

    private void sendSafely(long chatId, String text, Object keyboard) {
        sendSafely(new TelegramDestination(chatId, null, null), text, keyboard);
    }

    private void sendSafely(TelegramDestination destination, String text, Object keyboard) {
        try {
            api.sendMessage(destination, text, keyboard);
        } catch (RuntimeException exception) {
            log.warn("Chat {} ga xabar yuborilmadi: {}", destination.chatId(), exception.getMessage());
        }
    }

    private void editSafely(long chatId, int messageId, String text, Object keyboard) {
        try {
            api.editMessageText(chatId, messageId, text, keyboard);
        } catch (RuntimeException exception) {
            log.warn("Chat {} dagi xabar yangilanmadi: {}", chatId, exception.getMessage());
        }
    }

    private void answerSafely(String callbackId, String text, boolean alert) {
        try {
            api.answerCallback(callbackId, text, alert);
        } catch (RuntimeException exception) {
            log.warn("Callback javobi yuborilmadi: {}", exception.getMessage());
        }
    }

    private MediaAttachment mediaFrom(TelegramMessage message) {
        if (message.photo() != null && !message.photo().isEmpty()) {
            String fileId = message.photo().get(message.photo().size() - 1).fileId();
            return new MediaAttachment(ApplicationAttachmentType.PHOTO, fileId);
        }
        if (message.video() != null && message.video().fileId() != null) {
            return new MediaAttachment(ApplicationAttachmentType.VIDEO, message.video().fileId());
        }
        if (message.videoNote() != null && message.videoNote().fileId() != null) {
            return new MediaAttachment(ApplicationAttachmentType.VIDEO_NOTE, message.videoNote().fileId());
        }
        return null;
    }

    private void sendAttachmentSafely(long chatId, ApplicationAttachmentType type, String fileId) {
        sendAttachmentSafely(new TelegramDestination(chatId, null, null), type, fileId);
    }

    private void sendAttachmentSafely(
            TelegramDestination destination,
            ApplicationAttachmentType type,
            String fileId
    ) {
        try {
            api.sendAttachment(destination, type, fileId);
        } catch (RuntimeException exception) {
            log.warn("Chat {} ga media yuborilmadi: {}", destination.chatId(), exception.getMessage());
        }
    }

    private record MediaAttachment(ApplicationAttachmentType type, String fileId) {
    }
}
