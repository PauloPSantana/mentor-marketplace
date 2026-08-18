package br.com.mentorhub.notifications.application;

import br.com.mentorhub.feed.application.PostCommentedEvent;
import br.com.mentorhub.feed.application.PostLikedEvent;
import br.com.mentorhub.notifications.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CreateNotificationOnFeedActivity {

    private final CreateNotificationService createNotificationService;

    public CreateNotificationOnFeedActivity(CreateNotificationService createNotificationService) {
        this.createNotificationService = createNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePostLiked(PostLikedEvent event) {
        createNotificationService.execute(
                event.recipientUserId(),
                event.actorUserId(),
                NotificationType.POST_LIKED,
                event.postId(),
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePostCommented(PostCommentedEvent event) {
        NotificationType type = event.replyToComment()
                ? NotificationType.COMMENT_REPLIED
                : NotificationType.POST_COMMENTED;
        createNotificationService.execute(
                event.recipientUserId(),
                event.actorUserId(),
                type,
                event.postId(),
                event.commentId()
        );
    }
}
