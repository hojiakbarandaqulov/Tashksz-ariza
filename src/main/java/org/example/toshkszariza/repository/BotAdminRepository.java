package org.example.toshkszariza.repository;

import org.example.toshkszariza.domain.BotAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BotAdminRepository extends JpaRepository<BotAdmin, Integer> {
    boolean existsByTelegramUserId(long telegramUserId);
    Optional<BotAdmin> findByTelegramUserId(long telegramUserId);
    Optional<BotAdmin> findTopByOrderBySlotIdDesc();
    List<BotAdmin> findAllByOrderBySlotIdAsc();
}
