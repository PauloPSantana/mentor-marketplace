package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.domain.NotificationType;
import br.com.mentorhub.reviews.application.ReviewCreatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CreateNotificationOnReview {

    private final CreateNotificationService createNotificationService;

    public CreateNotificationOnReview(CreateNotificationService createNotificationService) {
        this.createNotificationService = createNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCreated(ReviewCreatedEvent event) {
        createNotificationService.execute(
                event.reviewedUserId(),
                event.reviewerUserId(),
                NotificationType.REVIEW_RECEIVED,
                null,
                null
        );
    }
}
