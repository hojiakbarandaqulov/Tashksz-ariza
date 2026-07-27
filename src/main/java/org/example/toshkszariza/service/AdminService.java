package org.example.toshkszariza.service;

import org.example.toshkszariza.config.BotProperties;
import org.example.toshkszariza.domain.BotAdmin;
import org.example.toshkszariza.domain.UserConversation;
import org.example.toshkszariza.repository.BotAdminRepository;
import org.example.toshkszariza.repository.UserConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private static final int SUPER_ADMIN_SLOT = 1;

    private final BotAdminRepository repository;
    private final UserConversationRepository conversationRepository;
    private final BotProperties properties;

    public AdminService(
            BotAdminRepository repository,
            UserConversationRepository conversationRepository,
            BotProperties properties
    ) {
        this.repository = repository;
        this.conversationRepository = conversationRepository;
        this.properties = properties;
    }

    /** Bo'sh bazada birinchi /start egasini atomar tarzda bosh admin qiladi. */
    @Transactional
    public synchronized boolean registerFirstAdminIfNeeded(long userId, long chatId, String username) {
        if (repository.existsById(SUPER_ADMIN_SLOT) || !properties.autoCreateAdmin()) {
            return false;
        }
        repository.save(new BotAdmin(SUPER_ADMIN_SLOT, userId, chatId, username));
        return true;
    }

    /** Sozlamadagi admin faqat bo'sh bazada bosh admin sifatida yaratiladi. */
    @Transactional
    public synchronized void registerConfiguredAdmin(long userId) {
        if (userId <= 0 || repository.existsByTelegramUserId(userId)) {
            return;
        }
        int slot = repository.existsById(SUPER_ADMIN_SLOT) ? nextSlot() : SUPER_ADMIN_SLOT;
        repository.save(new BotAdmin(slot, userId, userId, null));
    }

    /** Faqat bosh admin botni avval /start qilgan foydalanuvchini admin qila oladi. */
    @Transactional
    public synchronized AdminView addAdmin(long superAdminId, long newAdminId) {
        assertSuperAdmin(superAdminId);
        if (newAdminId <= 0) {
            throw new BotBusinessException("Telegram ID musbat raqam bo'lishi kerak.");
        }
        if (repository.existsByTelegramUserId(newAdminId)) {
            throw new BotBusinessException("Bu foydalanuvchi allaqachon administrator.");
        }
        UserConversation user = conversationRepository.findFirstByTelegramUserId(newAdminId)
                .orElseThrow(() -> new BotBusinessException(
                        "Bu foydalanuvchi topilmadi. U avval botga /start yuborsin."
                ));
        BotAdmin saved = repository.save(new BotAdmin(nextSlot(), newAdminId, user.getChatId(), null));
        return AdminView.from(saved);
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(long userId) {
        return repository.existsByTelegramUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isSuperAdmin(long userId) {
        return repository.findById(SUPER_ADMIN_SLOT)
                .map(admin -> admin.getTelegramUserId() == userId)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Long> adminChatIds() {
        return repository.findAllByOrderBySlotIdAsc().stream()
                .map(BotAdmin::getChatId)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> adminUserIds() {
        return repository.findAllByOrderBySlotIdAsc().stream()
                .map(BotAdmin::getTelegramUserId)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminView> listAdmins(long requesterId) {
        if (!isAdmin(requesterId)) {
            throw new BotBusinessException("Bu amal faqat administrator uchun.");
        }
        return repository.findAllByOrderBySlotIdAsc().stream().map(AdminView::from).toList();
    }

    private int nextSlot() {
        return repository.findTopByOrderBySlotIdDesc()
                .map(admin -> admin.getSlotId() + 1)
                .orElse(SUPER_ADMIN_SLOT);
    }

    private void assertSuperAdmin(long userId) {
        if (!isSuperAdmin(userId)) {
            throw new BotBusinessException("Bu amalni faqat bosh administrator bajara oladi.");
        }
    }
}
