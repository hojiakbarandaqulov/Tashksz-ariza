package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.ApplicationStatus;
import org.example.toshkszariza.domain.ApplicationAttachmentType;
import org.example.toshkszariza.domain.ConversationStep;
import org.example.toshkszariza.domain.ServiceApplication;
import org.example.toshkszariza.domain.UserConversation;
import org.example.toshkszariza.repository.ServiceApplicationRepository;
import org.example.toshkszariza.repository.UserConversationRepository;
import org.example.toshkszariza.service.flow.ConversationStepHandler;
import org.example.toshkszariza.service.flow.StepInput;
import org.example.toshkszariza.service.flow.StepResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationService {
    private final UserConversationRepository conversationRepository;
    private final ServiceApplicationRepository applicationRepository;
    private final Map<ConversationStep, ConversationStepHandler> handlers;

    public ConversationService(
            UserConversationRepository conversationRepository,
            ServiceApplicationRepository applicationRepository,
            List<ConversationStepHandler> stepHandlers
    ) {
        this.conversationRepository = conversationRepository;
        this.applicationRepository = applicationRepository;
        this.handlers = new EnumMap<>(ConversationStep.class);
        stepHandlers.forEach(handler -> handlers.put(handler.supportedStep(), handler));
    }

    @Transactional
    public ConversationStep startNew(long chatId, long userId) {
        UserConversation conversation = getOrCreate(chatId, userId);
        conversation.startNew();
        return conversation.getStep();
    }

    /** /start yuborgan foydalanuvchini broadcast ro'yxatiga ham kiritadi. */
    @Transactional
    public void registerUser(long chatId, long userId) {
        registerUser(chatId, userId, null, null, chatId == userId);
    }

    @Transactional
    public void registerUser(
            long chatId,
            long userId,
            Integer messageThreadId,
            Integer directMessagesTopicId,
            boolean privateChat
    ) {
        getOrCreate(chatId, userId).updateDestination(
                chatId,
                messageThreadId,
                directMessagesTopicId,
                privateChat
        );
    }

    @Transactional
    public void cancel(long chatId, long userId) {
        getOrCreate(chatId, userId).cancel();
    }

    @Transactional(readOnly = true)
    public ConversationStep currentStep(long userId) {
        return conversationRepository.findFirstByTelegramUserId(userId)
                .map(UserConversation::getStep)
                .orElse(ConversationStep.IDLE);
    }

    @Transactional
    public FlowOutcome handleInput(long chatId, long userId, String text, String contactPhone) {
        return handleInput(chatId, userId, text, contactPhone, null, null);
    }

    @Transactional
    public FlowOutcome handleInput(
            long chatId,
            long userId,
            String text,
            String contactPhone,
            ApplicationAttachmentType attachmentType,
            String attachmentFileId
    ) {
        UserConversation conversation = getOrCreate(chatId, userId);
        ConversationStepHandler handler = handlers.get(conversation.getStep());
        if (handler == null) {
            return new FlowOutcome(
                    StepResult.error(conversation.getStep(), "Avval «Yangi ariza» tugmasini bosing."),
                    toDraft(conversation)
            );
        }
        boolean singleFieldEdit = conversation.isSingleFieldEdit();
        StepResult result = handler.handle(
                conversation,
                new StepInput(text, contactPhone, attachmentType, attachmentFileId)
        );
        if (singleFieldEdit && result.accepted()) {
            // Tasdiqlash oynasidan tanlangan bitta maydon tahrirlangach, yana tasdiqlashga qaytamiz.
            conversation.finishSingleFieldEdit();
            result = StepResult.success(ConversationStep.CONFIRMING);
        }
        return new FlowOutcome(result, toDraft(conversation));
    }

    @Transactional
    public DraftView chooseField(long chatId, long userId, DraftField field) {
        UserConversation conversation = getOwnedConversation(chatId, userId);
        if (conversation.getStep() != ConversationStep.CONFIRMING) {
            throw new BotBusinessException("Hozir tahrirlash uchun tayyor ariza yo'q.");
        }
        conversation.beginSingleFieldEdit(field.step());
        return toDraft(conversation);
    }

    /** Tasdiqlash bosqichidagi tayyor arizaga media biriktiradi yoki uni almashtiradi. */
    @Transactional
    public DraftView attachMedia(
            long chatId,
            long userId,
            ApplicationAttachmentType attachmentType,
            String attachmentFileId
    ) {
        UserConversation conversation = getOwnedConversation(chatId, userId);
        if (conversation.getStep() != ConversationStep.CONFIRMING) {
            throw new BotBusinessException("Media biriktirish uchun ariza ma'lumotlarini avval kiriting.");
        }
        conversation.setAttachment(attachmentType, attachmentFileId);
        return toDraft(conversation);
    }

    @Transactional
    public DraftView loadCorrection(long chatId, long userId, long applicationId) {
        ServiceApplication application = findApplication(applicationId);
        if (application.getTelegramUserId() != userId || application.getUserChatId() != chatId) {
            throw new BotBusinessException("Bu arizani tahrirlashga ruxsat yo'q.");
        }
        if (application.getStatus() != ApplicationStatus.REJECTED) {
            throw new BotBusinessException("Faqat tuzatish uchun qaytarilgan arizani tahrirlash mumkin.");
        }
        UserConversation conversation = getOrCreate(chatId, userId);
        conversation.loadForCorrection(application);
        return toDraft(conversation);
    }

    @Transactional
    public ApplicationView submit(long chatId, long userId, String telegramUsername) {
        UserConversation conversation = getOwnedConversation(chatId, userId);
        if (conversation.getStep() != ConversationStep.CONFIRMING || !isComplete(conversation)) {
            throw new BotBusinessException("Ariza ma'lumotlari hali to'liq emas.");
        }

        ServiceApplication application;
        if (conversation.getEditingApplicationId() == null) {
            application = ServiceApplication.create(conversation, telegramUsername);
        } else {
            application = findApplication(conversation.getEditingApplicationId());
            if (application.getTelegramUserId() != userId) {
                throw new BotBusinessException("Bu arizani qayta yuborishga ruxsat yo'q.");
            }
            application.resubmit(conversation, telegramUsername);
        }
        application = applicationRepository.save(application);
        ApplicationView view = ApplicationView.from(application);
        conversation.submitted();
        return view;
    }

    @Transactional(readOnly = true)
    public DraftView draft(long chatId, long userId) {
        return toDraft(getOwnedConversation(chatId, userId));
    }

    private UserConversation getOrCreate(long chatId, long userId) {
        return conversationRepository.findFirstByTelegramUserId(userId)
                .orElseGet(() -> conversationRepository.save(new UserConversation(chatId, userId)));
    }

    private UserConversation getOwnedConversation(long chatId, long userId) {
        UserConversation conversation = conversationRepository.findFirstByTelegramUserId(userId)
                .orElseThrow(() -> new BotBusinessException("Faol ariza topilmadi."));
        if (conversation.getTelegramUserId() != userId) {
            throw new BotBusinessException("Suhbat egasi mos kelmadi.");
        }
        return conversation;
    }

    private ServiceApplication findApplication(long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BotBusinessException("Ariza topilmadi."));
    }

    private boolean isComplete(UserConversation conversation) {
        return conversation.getRegion() != null
                && conversation.getOrganizationName() != null
                && conversation.getDescription() != null;
    }

    private DraftView toDraft(UserConversation conversation) {
        return new DraftView(
                conversation.getFullName(),
                conversation.getPhone(),
                conversation.getOrganizationName(),
                conversation.getRegion(),
                conversation.getCategory(),
                conversation.getDescription(),
                conversation.getAttachmentType(),
                conversation.getAttachmentFileId(),
                conversation.getEditingApplicationId()
        );
    }
}
