package br.com.mentorhub.notifications.application;

import br.com.mentorhub.enrollments.application.MentorshipAcceptedEvent;
import br.com.mentorhub.enrollments.application.MentorshipCancelledEvent;
import br.com.mentorhub.enrollments.application.MentorshipRejectedEvent;
import br.com.mentorhub.enrollments.application.MentorshipRequestedEvent;
import br.com.mentorhub.notifications.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CreateNotificationOnMentorshipEnrollment {

    private final CreateNotificationService createNotificationService;

    public CreateNotificationOnMentorshipEnrollment(CreateNotificationService createNotificationService) {
        this.createNotificationService = createNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRequested(MentorshipRequestedEvent event) {
        createNotificationService.execute(
                event.mentorUserId(),
                event.menteeUserId(),
                NotificationType.MENTORSHIP_REQUESTED,
                null,
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleAccepted(MentorshipAcceptedEvent event) {
        createNotificationService.execute(
                event.menteeUserId(),
                event.mentorUserId(),
                NotificationType.MENTORSHIP_ACCEPTED,
                null,
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRejected(MentorshipRejectedEvent event) {
        createNotificationService.execute(
                event.menteeUserId(),
                event.mentorUserId(),
                NotificationType.MENTORSHIP_REJECTED,
                null,
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCancelled(MentorshipCancelledEvent event) {
        createNotificationService.execute(
                event.mentorUserId(),
                event.menteeUserId(),
                NotificationType.MENTORSHIP_CANCELLED,
                null,
                null
        );
    }
}
