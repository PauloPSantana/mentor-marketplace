package br.com.mentorhub.notifications.infrastructure.persistence;

import br.com.mentorhub.notifications.domain.Notification;
import br.com.mentorhub.notifications.domain.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final SpringDataNotificationRepository springDataNotificationRepository;

    public NotificationRepositoryImpl(SpringDataNotificationRepository springDataNotificationRepository) {
        this.springDataNotificationRepository = springDataNotificationRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return toDomain(springDataNotificationRepository.save(toEntity(notification)));
    }

    @Override
    public Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable) {
        return springDataNotificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId, pageable)
                .map(this::toDomain);
    }

    @Override
    public long countUnreadByRecipientUserId(UUID recipientUserId) {
        return springDataNotificationRepository.countByRecipientUserIdAndReadAtIsNull(recipientUserId);
    }

    @Override
    public Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId) {
        return springDataNotificationRepository.findByIdAndRecipientUserId(id, recipientUserId)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID recipientUserId) {
        springDataNotificationRepository.markAllAsRead(recipientUserId, Instant.now());
    }

    private NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId());
        entity.setRecipientUserId(notification.getRecipientUserId());
        entity.setActorUserId(notification.getActorUserId());
        entity.setType(notification.getType());
        entity.setPostId(notification.getPostId());
        entity.setCommentId(notification.getCommentId());
        entity.setReadAt(notification.getReadAt());
        entity.setCreatedAt(notification.getCreatedAt());
        return entity;
    }

    private Notification toDomain(NotificationJpaEntity entity) {
        return Notification.restore(
                entity.getId(),
                entity.getRecipientUserId(),
                entity.getActorUserId(),
                entity.getType(),
                entity.getPostId(),
                entity.getCommentId(),
                entity.getReadAt(),
                entity.getCreatedAt()
        );
    }
}
