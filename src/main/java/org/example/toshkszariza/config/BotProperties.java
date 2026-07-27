package org.example.toshkszariza.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Telegram botga tegishli barcha tashqi sozlamalar bitta joyda saqlanadi. */
@ConfigurationProperties(prefix = "telegram.bot")
public record BotProperties(
        String username,
        String token,
        long adminId,
        boolean autoCreateAdmin,
        boolean enabled,
        int longPollTimeoutSeconds,
        long pollDelayMs
) {
    public BotProperties {
        username = username == null ? "" : username.trim();
        token = token == null ? "" : token.trim();
        longPollTimeoutSeconds = longPollTimeoutSeconds <= 0 ? 25 : longPollTimeoutSeconds;
        pollDelayMs = pollDelayMs < 100 ? 300 : pollDelayMs;
    }

    public boolean isConfigured() {
        return enabled && !token.isBlank();
    }
}
