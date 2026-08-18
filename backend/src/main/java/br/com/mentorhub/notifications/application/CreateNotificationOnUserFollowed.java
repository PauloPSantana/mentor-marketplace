package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.domain.NotificationType;
import br.com.mentorhub.social.application.UserFollowedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CreateNotificationOnUserFollowed {

    private final CreateNotificationService createNotificationService;

    public CreateNotificationOnUserFollowed(CreateNotificationService createNotificationService) {
        this.createNotificationService = createNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(UserFollowedEvent event) {
        createNotificationService.execute(
                event.followedUserId(),
                event.followerUserId(),
                NotificationType.USER_FOLLOWED,
                null,
                null
        );
    }
}
