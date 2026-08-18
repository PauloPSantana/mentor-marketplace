package br.com.mentorhub.notifications.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    Page<NotificationJpaEntity> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    long countByRecipientUserIdAndReadAtIsNull(UUID recipientUserId);

    Optional<NotificationJpaEntity> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    @Modifying
    @Query("""
            UPDATE NotificationJpaEntity notification
            SET notification.readAt = :readAt
            WHERE notification.recipientUserId = :recipientUserId
              AND notification.readAt IS NULL
            """)
    void markAllAsRead(@Param("recipientUserId") UUID recipientUserId, @Param("readAt") Instant readAt);
}
