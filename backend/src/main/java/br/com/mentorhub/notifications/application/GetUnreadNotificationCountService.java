package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.api.dto.UnreadCountResponse;
import br.com.mentorhub.notifications.domain.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetUnreadNotificationCountService {

    private final NotificationRepository notificationRepository;

    public GetUnreadNotificationCountService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse execute(UUID recipientUserId) {
        return new UnreadCountResponse(notificationRepository.countUnreadByRecipientUserId(recipientUserId));
    }
}
