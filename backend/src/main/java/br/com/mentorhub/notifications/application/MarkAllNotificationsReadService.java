package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.domain.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MarkAllNotificationsReadService {

    private final NotificationRepository notificationRepository;

    public MarkAllNotificationsReadService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void execute(UUID recipientUserId) {
        notificationRepository.markAllAsRead(recipientUserId);
    }
}
