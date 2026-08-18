package br.com.mentorhub.notifications.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    long countUnreadByRecipientUserId(UUID recipientUserId);

    Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    void markAllAsRead(UUID recipientUserId);
}
