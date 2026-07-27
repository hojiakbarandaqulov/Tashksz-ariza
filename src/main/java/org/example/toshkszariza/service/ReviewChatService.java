package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.ReviewChatConfiguration;
import org.example.toshkszariza.repository.ReviewChatConfigurationRepository;
import org.example.toshkszariza.telegram.model.TelegramChat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReviewChatService {
    private static final int CONFIGURATION_SLOT = 1;

    private final ReviewChatConfigurationRepository repository;
    private final AdminService adminService;

    public ReviewChatService(ReviewChatConfigurationRepository repository, AdminService adminService) {
        this.repository = repository;
        this.adminService = adminService;
    }

    @Transactional
    public ReviewChatView bind(
            long requesterId,
            TelegramChat chat,
            Integer messageThreadId,
            Integer directMessagesTopicId
    ) {
        if (!adminService.isSuperAdmin(requesterId)) {
            throw new BotBusinessException("Guruh yoki kanalni faqat bosh admin bog'lay oladi.");
        }
        if (chat == null || chat.isPrivate()) {
            throw new BotBusinessException("/bind buyrug'ini kerakli guruh yoki kanal ichida yuboring.");
        }
        ReviewChatConfiguration configuration = new ReviewChatConfiguration(
                chat.id(),
                chat.type(),
                chat.title(),
                chat.username(),
                messageThreadId,
                directMessagesTopicId,
                requesterId
        );
        return ReviewChatView.from(repository.save(configuration));
    }

    @Transactional(readOnly = true)
    public Optional<ReviewChatView> configuredChat() {
        return repository.findById(CONFIGURATION_SLOT).map(ReviewChatView::from);
    }
}
