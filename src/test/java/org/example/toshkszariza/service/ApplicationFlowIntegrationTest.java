package org.example.toshkszariza.service;

import org.example.toshkszariza.domain.ApplicationStatus;
import org.example.toshkszariza.domain.ApplicationAttachmentType;
import org.example.toshkszariza.domain.ConversationStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.example.toshkszariza.telegram.model.TelegramChat;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ApplicationFlowIntegrationTest {
    private static final long USER_ID = 101L;
    private static final long CHAT_ID = 101L;
    private static final long ADMIN_ID = 999L;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private BroadcastService broadcastService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ApplicationStatisticsService statisticsService;

    @Autowired
    private ReviewChatService reviewChatService;

    @Test
    void applicationCanBeRejectedCorrectedResubmittedAndAccepted() {
        conversationService.startNew(CHAT_ID, USER_ID);
        assertStep("Chilonzor KSZ", ConversationStep.WAITING_APPLICATION_DETAILS);
        assertStep(
                "Toshkent shahar korxonasi\n+998 90 123 45 67\nElektr tarmog'ida uzilish kuzatilmoqda.",
                ConversationStep.CONFIRMING
        );

        ApplicationView firstSubmission = conversationService.submit(CHAT_ID, USER_ID, "anvar");
        assertThat(firstSubmission.phone()).isEqualTo("+998 90 123 45 67");
        assertThat(firstSubmission.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(firstSubmission.revision()).isEqualTo(1);
        assertThat(applicationService.userApplications(USER_ID))
                .extracting(ApplicationView::id)
                .contains(firstSubmission.id());
        assertThat(applicationService.userApplications(202L)).isEmpty();

        applicationService.beginRejection(ADMIN_ID, firstSubmission.id(), ADMIN_ID, 77);
        RejectionResult rejection = applicationService.rejectWithReason(
                ADMIN_ID,
                "Manzil va avariya tafsilotlarini aniqroq yozing."
        );
        assertThat(rejection.application().status()).isEqualTo(ApplicationStatus.REJECTED);

        conversationService.loadCorrection(CHAT_ID, USER_ID, firstSubmission.id());
        conversationService.chooseField(CHAT_ID, USER_ID, DraftField.DESCRIPTION);
        FlowOutcome corrected = conversationService.handleInput(
                CHAT_ID,
                USER_ID,
                "Chilonzor tumani, Bunyodkor ko'chasidagi elektr tarmog'ida uzilish bor.",
                null
        );
        assertThat(corrected.result().currentStep()).isEqualTo(ConversationStep.CONFIRMING);

        ApplicationView secondSubmission = conversationService.submit(CHAT_ID, USER_ID, "anvar");
        assertThat(secondSubmission.id()).isEqualTo(firstSubmission.id());
        assertThat(secondSubmission.revision()).isEqualTo(2);
        assertThat(secondSubmission.status()).isEqualTo(ApplicationStatus.PENDING);

        ApplicationView accepted = applicationService.accept(ADMIN_ID, secondSubmission.id());
        assertThat(accepted.status()).isEqualTo(ApplicationStatus.ACCEPTED);

        conversationService.registerUser(202L, 202L);
        broadcastService.begin(ADMIN_ID);
        BroadcastPreview preview = broadcastService.prepare(ADMIN_ID, "Bugun texnik ishlar amalga oshiriladi.");
        assertThat(preview.recipientCount()).isEqualTo(2);

        BroadcastDelivery delivery = broadcastService.confirm(ADMIN_ID);
        assertThat(delivery.recipientChatIds()).containsExactlyInAnyOrder(USER_ID, 202L);
        assertThat(delivery.recipientChatIds()).doesNotContain(ADMIN_ID);

        // Birinchi arizadan keyin hudud qayta so'ralmaydi.
        assertThat(conversationService.startNew(CHAT_ID, USER_ID))
                .isEqualTo(ConversationStep.WAITING_APPLICATION_DETAILS);
        conversationService.cancel(CHAT_ID, USER_ID);

        // Bosh admin botni avval ochgan foydalanuvchini qo'shimcha admin qila oladi.
        conversationService.registerUser(303L, 303L);
        AdminView addedAdmin = adminService.addAdmin(ADMIN_ID, 303L);
        assertThat(addedAdmin.telegramUserId()).isEqualTo(303L);
        assertThat(adminService.isAdmin(303L)).isTrue();
        assertThat(adminService.isSuperAdmin(303L)).isFalse();
    }

    @Test
    void photoVideoAndVideoNoteCanBeSubmittedWithApplication() {
        assertMediaApplication(401L, ApplicationAttachmentType.PHOTO, "photo-file-id", "Photo company");
        assertMediaApplication(402L, ApplicationAttachmentType.VIDEO, "video-file-id", "Video company");

        long videoNoteUser = 403L;
        conversationService.startNew(videoNoteUser, videoNoteUser);
        assertStep(videoNoteUser, "Bektemir KSZ", ConversationStep.WAITING_APPLICATION_DETAILS);
        FlowOutcome videoNote = conversationService.handleInput(
                videoNoteUser,
                videoNoteUser,
                "",
                null,
                ApplicationAttachmentType.VIDEO_NOTE,
                "video-note-file-id"
        );
        assertThat(videoNote.result().currentStep()).isEqualTo(ConversationStep.CONFIRMING);
        ApplicationView submitted = conversationService.submit(videoNoteUser, videoNoteUser, "video_note_user");
        assertThat(submitted.phone()).isNull();
        assertThat(submitted.attachmentType()).isEqualTo(ApplicationAttachmentType.VIDEO_NOTE);
        assertThat(submitted.attachmentFileId()).isEqualTo("video-note-file-id");

        WeeklyStatistics statistics = statisticsService.lastSevenDays(ADMIN_ID);
        assertThat(statistics.total()).isEqualTo(3);
    }

    @Test
    void groupAndTopicDestinationsAreStoredPerUser() {
        long groupId = -1001234567890L;
        reviewChatService.bind(
                ADMIN_ID,
                new TelegramChat(groupId, "supergroup", "TOSHKSZ Ariza", "TOSHKSZBD"),
                null,
                null
        );
        assertThat(reviewChatService.configuredChat().map(ReviewChatView::chatId)).contains(groupId);

        long topicUser = 501L;
        conversationService.registerUser(groupId, topicUser, 77, null, false);
        conversationService.startNew(groupId, topicUser);
        assertStep(topicUser, "Bektemir KSZ", ConversationStep.WAITING_APPLICATION_DETAILS);
        assertStep(topicUser,
                "Topic company\n+998 90 111 22 33\nGuruh mavzusidagi ariza matni.",
                ConversationStep.CONFIRMING);
        ApplicationView topicApplication = conversationService.submit(groupId, topicUser, "topic_user");
        assertThat(topicApplication.userChatId()).isEqualTo(groupId);
        assertThat(topicApplication.userMessageThreadId()).isEqualTo(77);

        long directMessageUser = 502L;
        conversationService.registerUser(groupId, directMessageUser, null, 88, false);
        conversationService.startNew(groupId, directMessageUser);
        assertStep(directMessageUser, "Sergeli KSZ", ConversationStep.WAITING_APPLICATION_DETAILS);
        assertStep(directMessageUser,
                "DM company\n+998 91 222 33 44\nKanal direct messages arizasi.",
                ConversationStep.CONFIRMING);
        ApplicationView directApplication = conversationService.submit(
                groupId,
                directMessageUser,
                "direct_message_user"
        );
        assertThat(directApplication.userChatId()).isEqualTo(groupId);
        assertThat(directApplication.userDirectMessagesTopicId()).isEqualTo(88);
        assertThat(conversationService.currentStep(99999L)).isEqualTo(ConversationStep.IDLE);
    }

    private void assertMediaApplication(
            long userId,
            ApplicationAttachmentType type,
            String fileId,
            String company
    ) {
        conversationService.startNew(userId, userId);
        assertStep(userId, "Bektemir KSZ", ConversationStep.WAITING_APPLICATION_DETAILS);
        FlowOutcome media = conversationService.handleInput(
                userId,
                userId,
                company + "\n+998 90 555 66 77",
                null,
                type,
                fileId
        );
        assertThat(media.result().currentStep()).isEqualTo(ConversationStep.CONFIRMING);

        ApplicationView submitted = conversationService.submit(userId, userId, "media_user");
        assertThat(submitted.organizationName()).isEqualTo(company);
        assertThat(submitted.phone()).isEqualTo("+998 90 555 66 77");
        assertThat(submitted.attachmentType()).isEqualTo(type);
        assertThat(submitted.attachmentFileId()).isEqualTo(fileId);
    }

    @Test
    void twoLineFormatIsConfirmedWithoutAskingPhone() {
        long userId = 601L;
        conversationService.startNew(userId, userId);
        assertStep(userId, "Uchtepa KSZ", ConversationStep.WAITING_APPLICATION_DETAILS);
        assertStep(
                userId,
                "Eski format korxonasi\nTransformator yonida texnik nosozlik bor.",
                ConversationStep.CONFIRMING
        );

        ApplicationView submitted = conversationService.submit(userId, userId, "legacy_user");
        assertThat(submitted.phone()).isNull();
    }

    @Test
    void applicationDetailsDoNotRequireExactlyThreeLinesOrPlus998() {
        long userId = 602L;
        conversationService.startNew(userId, userId);
        assertStep(userId, "Yunusobod KSZ", ConversationStep.WAITING_APPLICATION_DETAILS);

        assertStep(userId,
                "6 blok yonida transformatorda nosozlik",
                ConversationStep.CONFIRMING);

        ApplicationView submitted = conversationService.submit(userId, userId, "flexible_user");
        assertThat(submitted.organizationName()).isEqualTo("Ko'rsatilmagan");
        assertThat(submitted.description()).isEqualTo("6 blok yonida transformatorda nosozlik");
        assertThat(submitted.phone()).isNull();
    }

    @Test
    void allApplicationDetailsCanBeSentOnOneLine() {
        long userId = 603L;
        conversationService.startNew(userId, userId);
        assertStep(userId, "Bektemir KSZ", ConversationStep.WAITING_APPLICATION_DETAILS);
        assertStep(
                userId,
                "Bir qator korxona; Transformator qizib ketmoqda; 71 200 00 00",
                ConversationStep.CONFIRMING
        );

        ApplicationView submitted = conversationService.submit(userId, userId, "one_line_user");
        assertThat(submitted.organizationName()).isEqualTo("Bir qator korxona");
        assertThat(submitted.description()).isEqualTo("Transformator qizib ketmoqda");
        assertThat(submitted.phone()).isEqualTo("71 200 00 00");
    }

    private void assertStep(String input, ConversationStep expectedStep) {
        assertStep(CHAT_ID, input, expectedStep);
    }

    private void assertStep(long userId, String input, ConversationStep expectedStep) {
        FlowOutcome outcome = conversationService.handleInput(userId, userId, input, null);
        assertThat(outcome.result().accepted()).isTrue();
        assertThat(outcome.result().currentStep()).isEqualTo(expectedStep);
    }
}
