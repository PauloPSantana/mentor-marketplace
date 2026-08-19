package br.com.mentorhub.notifications.application;

import br.com.mentorhub.mentorships.application.MentorshipCompletedEvent;
import br.com.mentorhub.mentorships.application.SessionCancelledEvent;
import br.com.mentorhub.mentorships.application.SessionCompletedEvent;
import br.com.mentorhub.mentorships.application.SessionCreatedEvent;
import br.com.mentorhub.mentorships.application.SessionNoShowEvent;
import br.com.mentorhub.mentorships.application.SessionReminderEvent;
import br.com.mentorhub.notifications.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class CreateNotificationOnMentorshipSession {

    private final CreateNotificationService createNotificationService;

    public CreateNotificationOnMentorshipSession(CreateNotificationService createNotificationService) {
        this.createNotificationService = createNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCreated(SessionCreatedEvent event) {
        notifyBoth(event.mentorUserId(), event.menteeUserId(), event.actorUserId(), NotificationType.SESSION_CREATED);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCancelled(SessionCancelledEvent event) {
        notifyBoth(event.mentorUserId(), event.menteeUserId(), event.actorUserId(), NotificationType.SESSION_CANCELLED);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCompleted(SessionCompletedEvent event) {
        notifyBoth(event.mentorUserId(), event.menteeUserId(), event.actorUserId(), NotificationType.SESSION_COMPLETED);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleNoShow(SessionNoShowEvent event) {
        notifyBoth(event.mentorUserId(), event.menteeUserId(), event.actorUserId(), NotificationType.SESSION_NO_SHOW);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleReminder(SessionReminderEvent event) {
        createNotificationService.execute(
                event.menteeUserId(),
                event.mentorUserId(),
                NotificationType.SESSION_REMINDER,
                null,
                null
        );
        createNotificationService.execute(
                event.mentorUserId(),
                event.menteeUserId(),
                NotificationType.SESSION_REMINDER,
                null,
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleMentorshipCompleted(MentorshipCompletedEvent event) {
        createNotificationService.execute(
                event.menteeUserId(),
                event.mentorUserId(),
                NotificationType.MENTORSHIP_COMPLETED,
                null,
                null
        );
        createNotificationService.execute(
                event.mentorUserId(),
                event.menteeUserId(),
                NotificationType.MENTORSHIP_COMPLETED,
                null,
                null
        );
    }

    private void notifyBoth(UUID mentorUserId, UUID menteeUserId, UUID actorUserId, NotificationType type) {
        createNotificationService.execute(mentorUserId, actorUserId, type, null, null);
        createNotificationService.execute(menteeUserId, actorUserId, type, null, null);
    }
}
