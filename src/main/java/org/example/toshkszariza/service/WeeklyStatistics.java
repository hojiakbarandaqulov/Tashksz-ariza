package org.example.toshkszariza.service;

import java.time.Instant;

public record WeeklyStatistics(
        Instant since,
        Instant until,
        long total,
        long pending,
        long accepted,
        long rejected
) {
}
