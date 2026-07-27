package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.AdminAddSession;
import org.example.toshkszariza.repository.AdminAddSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminManagementService {
    private final AdminAddSessionRepository sessionRepository;
    private final AdminService adminService;

    public AdminManagementService(AdminAddSessionRepository sessionRepository, AdminService adminService) {
        this.sessionRepository = sessionRepository;
        this.adminService = adminService;
    }

    @Transactional
    public void begin(long superAdminId) {
        if (!adminService.isSuperAdmin(superAdminId)) {
            throw new BotBusinessException("Bu amalni faqat bosh administrator bajara oladi.");
        }
        sessionRepository.save(new AdminAddSession(superAdminId));
    }

    @Transactional(readOnly = true)
    public boolean isWaiting(long superAdminId) {
        return sessionRepository.existsById(superAdminId);
    }

    @Transactional
    public AdminView complete(long superAdminId, String rawTelegramId) {
        if (!sessionRepository.existsById(superAdminId)) {
            throw new BotBusinessException("Admin qo'shish amali boshlanmagan.");
        }
        long newAdminId;
        try {
            newAdminId = Long.parseLong(rawTelegramId == null ? "" : rawTelegramId.trim());
        } catch (NumberFormatException exception) {
            throw new BotBusinessException("Telegram ID faqat raqamlardan iborat bo'lishi kerak.");
        }
        AdminView added = adminService.addAdmin(superAdminId, newAdminId);
        sessionRepository.deleteById(superAdminId);
        return added;
    }

    @Transactional
    public void cancel(long superAdminId) {
        sessionRepository.deleteById(superAdminId);
    }
}
