package org.example.toshkszariza.repository;

import org.example.toshkszariza.domain.ApplicationStatus;
import org.example.toshkszariza.domain.ServiceApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.Instant;

public interface ServiceApplicationRepository extends JpaRepository<ServiceApplication, Long> {
    List<ServiceApplication> findTop10ByTelegramUserIdOrderByCreatedAtDesc(long telegramUserId);

    List<ServiceApplication> findTop10ByStatusOrderBySubmittedAtAsc(ApplicationStatus status);

    long countByStatus(ApplicationStatus status);

    long countByCreatedAtGreaterThanEqual(Instant since);

    long countByStatusAndCreatedAtGreaterThanEqual(ApplicationStatus status, Instant since);
}
