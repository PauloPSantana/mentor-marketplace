package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.notifications.application.CreateNotificationService;
import br.com.mentorhub.notifications.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CreateNotificationOnStudyPlan {

    private final CreateNotificationService createNotificationService;

    public CreateNotificationOnStudyPlan(CreateNotificationService createNotificationService) {
        this.createNotificationService = createNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleAssigned(StudyTaskAssignedEvent event) {
        createNotificationService.execute(
                event.menteeUserId(),
                event.actorUserId(),
                NotificationType.STUDY_TASK_ASSIGNED,
                null,
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCompleted(StudyPlanCompletedEvent event) {
        createNotificationService.execute(
                event.mentorUserId(),
                event.menteeUserId(),
                NotificationType.STUDY_PLAN_COMPLETED,
                null,
                null
        );
    }
}
