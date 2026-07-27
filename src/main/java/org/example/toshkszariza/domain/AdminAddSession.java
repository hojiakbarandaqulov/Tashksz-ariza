package org.example.toshkszariza.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Bosh admin yangi administratorning Telegram ID raqamini yozishini kutayotgan holat. */
@Entity
@Table(name = "admin_add_sessions")
public class AdminAddSession {
    @Id
    @Column(name = "super_admin_id")
    private Long superAdminId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AdminAddSession() {
    }

    public AdminAddSession(long superAdminId) {
        this.superAdminId = superAdminId;
        this.createdAt = Instant.now();
    }
}
