package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.domain.Notification;
import br.com.mentorhub.notifications.domain.NotificationRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class MarkNotificationReadService {

    private final NotificationRepository notificationRepository;

    public MarkNotificationReadService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void execute(UUID notificationId, UUID recipientUserId) {
        Notification notification = notificationRepository.findByIdAndRecipientUserId(notificationId, recipientUserId)
                .orElseThrow(() -> new NotFoundException("Notificação não encontrada"));
        notification.markAsRead(Instant.now());
        notificationRepository.save(notification);
    }
}
