package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.api.dto.NotificationFeedResponse;
import br.com.mentorhub.notifications.api.dto.NotificationResponse;
import br.com.mentorhub.notifications.domain.Notification;
import br.com.mentorhub.notifications.domain.NotificationRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListNotificationsService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ListNotificationsService(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public NotificationFeedResponse execute(UUID recipientUserId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Page<Notification> notifications = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(
                recipientUserId,
                PageRequest.of(safePage, safeSize)
        );

        List<UUID> actorIds = notifications.getContent().stream()
                .map(Notification::getActorUserId)
                .distinct()
                .toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(actorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<NotificationResponse> items = notifications.getContent().stream()
                .map(notification -> {
                    User actor = usersById.get(notification.getActorUserId());
                    String actorName = actor != null ? actor.getName() : "Usuário";
                    return NotificationResponse.from(notification, actorName);
                })
                .toList();

        long unreadCount = notificationRepository.countUnreadByRecipientUserId(recipientUserId);

        return new NotificationFeedResponse(
                items,
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages(),
                notifications.isLast(),
                unreadCount
        );
    }
}
