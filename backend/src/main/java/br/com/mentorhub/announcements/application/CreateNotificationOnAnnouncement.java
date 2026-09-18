package br.com.mentorhub.announcements.application;

import br.com.mentorhub.notifications.application.CreateNotificationService;
import br.com.mentorhub.notifications.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class CreateNotificationOnAnnouncement {

    private final CreateNotificationService createNotificationService;

    public CreateNotificationOnAnnouncement(CreateNotificationService createNotificationService) {
        this.createNotificationService = createNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePublished(AnnouncementPublishedEvent event) {
        if (event.recipientUserIds() == null) {
            return;
        }
        for (UUID recipient : event.recipientUserIds()) {
            createNotificationService.execute(
                    recipient,
                    event.actorUserId(),
                    NotificationType.ANNOUNCEMENT_PUBLISHED,
                    null,
                    null
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleLiked(AnnouncementLikedEvent event) {
        createNotificationService.execute(
                event.authorUserId(),
                event.actorUserId(),
                NotificationType.ANNOUNCEMENT_LIKED,
                null,
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCommented(AnnouncementCommentedEvent event) {
        if (event.parentAuthorUserId() != null) {
            createNotificationService.execute(
                    event.parentAuthorUserId(),
                    event.actorUserId(),
                    NotificationType.ANNOUNCEMENT_REPLIED,
                    null,
                    null
            );
            return;
        }
        createNotificationService.execute(
                event.authorUserId(),
                event.actorUserId(),
                NotificationType.ANNOUNCEMENT_COMMENTED,
                null,
                null
        );
    }
}
