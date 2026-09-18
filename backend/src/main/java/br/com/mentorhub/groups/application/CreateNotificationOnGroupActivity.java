package br.com.mentorhub.groups.application;

import br.com.mentorhub.groups.domain.GroupMember;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.notifications.application.CreateNotificationService;
import br.com.mentorhub.notifications.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class CreateNotificationOnGroupActivity {

    private final CreateNotificationService createNotificationService;
    private final MentorshipGroupRepository mentorshipGroupRepository;

    public CreateNotificationOnGroupActivity(
            CreateNotificationService createNotificationService,
            MentorshipGroupRepository mentorshipGroupRepository
    ) {
        this.createNotificationService = createNotificationService;
        this.mentorshipGroupRepository = mentorshipGroupRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCreated(GroupCreatedEvent event) {
        MentorshipGroup group = mentorshipGroupRepository.findById(event.groupId()).orElse(null);
        if (group == null) {
            return;
        }
        for (GroupMember member : group.getMembers()) {
            createNotificationService.execute(
                    member.getUserId(),
                    event.actorUserId(),
                    NotificationType.GROUP_CREATED,
                    null,
                    null
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleAdded(GroupMemberAddedEvent event) {
        createNotificationService.execute(
                event.memberUserId(),
                event.actorUserId(),
                NotificationType.GROUP_MEMBER_ADDED,
                null,
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleLeft(GroupMemberLeftEvent event) {
        UUID recipient = event.memberUserId().equals(event.actorUserId())
                ? event.ownerUserId()
                : event.memberUserId();
        createNotificationService.execute(
                recipient,
                event.actorUserId(),
                NotificationType.GROUP_MEMBER_LEFT,
                null,
                null
        );
    }
}
