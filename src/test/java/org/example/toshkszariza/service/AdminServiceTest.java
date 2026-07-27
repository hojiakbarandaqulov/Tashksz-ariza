package org.example.toshkszariza.service;

import org.example.toshkszariza.config.BotProperties;
import org.example.toshkszariza.domain.BotAdmin;
import org.example.toshkszariza.repository.BotAdminRepository;
import org.example.toshkszariza.repository.UserConversationRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminServiceTest {
    private final BotAdminRepository repository = mock(BotAdminRepository.class);
    private final UserConversationRepository conversationRepository = mock(UserConversationRepository.class);
    private final BotProperties properties = new BotProperties(
            "test_bot", "test-token", 0, true, true, 25, 300
    );
    private final AdminService service = new AdminService(repository, conversationRepository, properties);

    @Test
    void firstStartCreatesSuperAdmin() {
        when(repository.existsById(1)).thenReturn(false);

        boolean created = service.registerFirstAdminIfNeeded(101, 101, "owner");

        assertThat(created).isTrue();
        verify(repository).save(any(BotAdmin.class));
    }

    @Test
    void existingSuperAdminCannotBeReplacedByNextUser() {
        when(repository.existsById(1)).thenReturn(true);

        boolean created = service.registerFirstAdminIfNeeded(202, 202, "another_user");

        assertThat(created).isFalse();
        verify(repository, never()).save(any(BotAdmin.class));
    }
}
