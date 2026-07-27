package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.AdminReviewSession;
import org.example.toshkszariza.domain.ApplicationStatus;
import org.example.toshkszariza.domain.ServiceApplication;
import org.example.toshkszariza.repository.AdminReviewSessionRepository;
import org.example.toshkszariza.repository.ServiceApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    private final ServiceApplicationRepository applicationRepository;
    private final AdminReviewSessionRepository sessionRepository;
    private final InputValidator validator;
    private final AdminService adminService;

    public ApplicationService(
            ServiceApplicationRepository applicationRepository,
            AdminReviewSessionRepository sessionRepository,
            InputValidator validator,
            AdminService adminService
    ) {
        this.applicationRepository = applicationRepository;
        this.sessionRepository = sessionRepository;
        this.validator = validator;
        this.adminService = adminService;
    }

    @Transactional(readOnly = true)
    public List<ApplicationView> userApplications(long userId) {
        return applicationRepository.findTop10ByTelegramUserIdOrderByCreatedAtDesc(userId)
                .stream().map(ApplicationView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationView> pendingApplications(long adminId) {
        assertAdmin(adminId);
        return applicationRepository.findTop10ByStatusOrderBySubmittedAtAsc(ApplicationStatus.PENDING)
                .stream().map(ApplicationView::from).toList();
    }

    @Transactional(readOnly = true)
    public long pendingCount(long adminId) {
        assertAdmin(adminId);
        return applicationRepository.countByStatus(ApplicationStatus.PENDING);
    }

    @Transactional
    public ApplicationView accept(long adminId, long applicationId) {
        assertAdmin(adminId);
        ServiceApplication application = requirePending(applicationId);
        application.accept(adminId);
        sessionRepository.findById(adminId)
                .filter(session -> session.getApplicationId().equals(applicationId))
                .ifPresent(sessionRepository::delete);
        return ApplicationView.from(application);
    }

    @Transactional
    public void beginRejection(
            long adminId,
            long applicationId,
            long notificationChatId,
            int notificationMessageId
    ) {
        assertAdmin(adminId);
        requirePending(applicationId);
        sessionRepository.save(new AdminReviewSession(
                adminId, applicationId, notificationChatId, notificationMessageId
        ));
    }

    @Transactional(readOnly = true)
    public boolean isWaitingForReason(long adminId) {
        return sessionRepository.existsById(adminId);
    }

    @Transactional
    public RejectionResult rejectWithReason(long adminId, String rawReason) {
        assertAdmin(adminId);
        AdminReviewSession session = sessionRepository.findById(adminId)
                .orElseThrow(() -> new BotBusinessException("Rad etilayotgan ariza tanlanmagan."));
        String reason = validator.clean(rawReason);
        if (reason.length() < 3 || reason.length() > 1000) {
            throw new BotBusinessException("Rad etish sababini 3–1000 belgi oralig'ida yozing.");
        }
        ServiceApplication application = requirePending(session.getApplicationId());
        application.reject(adminId, reason);
        RejectionResult result = new RejectionResult(
                ApplicationView.from(application),
                session.getNotificationChatId(),
                session.getNotificationMessageId()
        );
        sessionRepository.delete(session);
        return result;
    }

    @Transactional
    public boolean cancelRejection(long adminId) {
        Optional<AdminReviewSession> session = sessionRepository.findById(adminId);
        session.ifPresent(sessionRepository::delete);
        return session.isPresent();
    }

    public boolean isAdmin(long userId) {
        return adminService.isAdmin(userId);
    }

    private void assertAdmin(long adminId) {
        if (!isAdmin(adminId)) {
            throw new BotBusinessException("Bu amal faqat administrator uchun.");
        }
    }

    private ServiceApplication requirePending(long applicationId) {
        ServiceApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BotBusinessException("Ariza topilmadi."));
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BotBusinessException("Ariza allaqachon ko'rib chiqilgan.");
        }
        return application;
    }
}
