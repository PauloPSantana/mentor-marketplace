package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.domain.Notification;
import br.com.mentorhub.notifications.domain.NotificationRepository;
import br.com.mentorhub.notifications.domain.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateNotificationService {

    private final NotificationRepository notificationRepository;

    public CreateNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void execute(
            UUID recipientUserId,
            UUID actorUserId,
            NotificationType type,
            UUID postId,
            UUID commentId
    ) {
        if (recipientUserId.equals(actorUserId)) {
            return;
        }
        notificationRepository.save(Notification.create(recipientUserId, actorUserId, type, postId, commentId));
    }
}
