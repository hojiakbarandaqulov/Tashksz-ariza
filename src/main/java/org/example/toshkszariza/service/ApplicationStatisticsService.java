package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.ApplicationStatus;
import org.example.toshkszariza.repository.ServiceApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class ApplicationStatisticsService {
    private final ServiceApplicationRepository repository;
    private final AdminService adminService;

    public ApplicationStatisticsService(ServiceApplicationRepository repository, AdminService adminService) {
        this.repository = repository;
        this.adminService = adminService;
    }

    @Transactional(readOnly = true)
    public WeeklyStatistics lastSevenDays(long requesterId) {
        if (!adminService.isAdmin(requesterId)) {
            throw new BotBusinessException("Bu amal faqat administrator uchun.");
        }
        return calculate();
    }

    @Transactional(readOnly = true)
    public WeeklyStatistics calculate() {
        Instant until = Instant.now();
        Instant since = until.minus(7, ChronoUnit.DAYS);
        return new WeeklyStatistics(
                since,
                until,
                repository.countByCreatedAtGreaterThanEqual(since),
                repository.countByStatusAndCreatedAtGreaterThanEqual(ApplicationStatus.PENDING, since),
                repository.countByStatusAndCreatedAtGreaterThanEqual(ApplicationStatus.ACCEPTED, since),
                repository.countByStatusAndCreatedAtGreaterThanEqual(ApplicationStatus.REJECTED, since)
        );
    }
}
