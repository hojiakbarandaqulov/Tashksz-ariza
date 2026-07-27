package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.AdminBroadcastSession;
import org.example.toshkszariza.repository.AdminBroadcastSessionRepository;
import org.example.toshkszariza.repository.UserConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class BroadcastService {
    private final AdminBroadcastSessionRepository sessionRepository;
    private final UserConversationRepository conversationRepository;
    private final AdminService adminService;

    public BroadcastService(
            AdminBroadcastSessionRepository sessionRepository,
            UserConversationRepository conversationRepository,
            AdminService adminService
    ) {
        this.sessionRepository = sessionRepository;
        this.conversationRepository = conversationRepository;
        this.adminService = adminService;
    }

    @Transactional
    public void begin(long adminId) {
        assertAdmin(adminId);
        sessionRepository.save(new AdminBroadcastSession(adminId));
    }

    @Transactional(readOnly = true)
    public boolean isWaitingForText(long adminId) {
        return sessionRepository.findById(adminId)
                .map(session -> !session.isPrepared())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isActive(long adminId) {
        return sessionRepository.existsById(adminId);
    }

    @Transactional
    public BroadcastPreview prepare(long adminId, String rawMessage) {
        assertAdmin(adminId);
        AdminBroadcastSession session = requireSession(adminId);
        String message = rawMessage == null ? "" : rawMessage.trim();
        if (message.isBlank() || message.length() > 3500) {
            throw new BotBusinessException("Xabar 1–3500 belgi oralig'ida bo'lishi kerak.");
        }
        session.prepare(message);
        int recipients = recipientChatIds().size();
        return new BroadcastPreview(message, recipients);
    }

    @Transactional
    public BroadcastDelivery confirm(long adminId) {
        assertAdmin(adminId);
        AdminBroadcastSession session = requireSession(adminId);
        if (!session.isPrepared()) {
            throw new BotBusinessException("Avval yuboriladigan xabarni yozing.");
        }
        BroadcastDelivery delivery = new BroadcastDelivery(
                session.getMessageText(),
                recipientChatIds()
        );
        sessionRepository.delete(session);
        return delivery;
    }

    @Transactional
    public boolean cancel(long adminId) {
        var session = sessionRepository.findById(adminId);
        session.ifPresent(sessionRepository::delete);
        return session.isPresent();
    }

    private AdminBroadcastSession requireSession(long adminId) {
        return sessionRepository.findById(adminId)
                .orElseThrow(() -> new BotBusinessException("Umumiy xabar sessiyasi topilmadi."));
    }

    private void assertAdmin(long userId) {
        if (!adminService.isAdmin(userId)) {
            throw new BotBusinessException("Bu amal faqat administrator uchun.");
        }
    }

    /** Umumiy xabar oddiy foydalanuvchilarga yuboriladi, adminlar ro'yxatdan chiqariladi. */
    private List<Long> recipientChatIds() {
        var adminChats = new HashSet<>(adminService.adminChatIds());
        return conversationRepository.findRecipientChatIds().stream()
                .filter(chatId -> !adminChats.contains(chatId))
                .toList();
    }
}
