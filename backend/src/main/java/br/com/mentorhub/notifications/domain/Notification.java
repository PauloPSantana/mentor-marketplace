package br.com.mentorhub.notifications.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Notification {

    private final UUID id;
    private final UUID recipientUserId;
    private final UUID actorUserId;
    private final NotificationType type;
    private final UUID postId;
    private final UUID commentId;
    private Instant readAt;
    private final Instant createdAt;

    private Notification(
            UUID id,
            UUID recipientUserId,
            UUID actorUserId,
            NotificationType type,
            UUID postId,
            UUID commentId,
            Instant readAt,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.recipientUserId = Objects.requireNonNull(recipientUserId);
        this.actorUserId = Objects.requireNonNull(actorUserId);
        this.type = Objects.requireNonNull(type);
        this.postId = postId;
        this.commentId = commentId;
        this.readAt = readAt;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Notification create(
            UUID recipientUserId,
            UUID actorUserId,
            NotificationType type,
            UUID postId,
            UUID commentId
    ) {
        if (recipientUserId.equals(actorUserId)) {
            throw new IllegalArgumentException("Notificação não pode ser enviada ao próprio autor da ação");
        }
        return new Notification(
                UUID.randomUUID(),
                recipientUserId,
                actorUserId,
                type,
                postId,
                commentId,
                null,
                Instant.now()
        );
    }

    public static Notification restore(
            UUID id,
            UUID recipientUserId,
            UUID actorUserId,
            NotificationType type,
            UUID postId,
            UUID commentId,
            Instant readAt,
            Instant createdAt
    ) {
        return new Notification(id, recipientUserId, actorUserId, type, postId, commentId, readAt, createdAt);
    }

    public void markAsRead(Instant readAt) {
        if (this.readAt == null) {
            this.readAt = Objects.requireNonNull(readAt);
        }
    }

    public boolean isRead() {
        return readAt != null;
    }

    public boolean isOwnedBy(UUID userId) {
        return recipientUserId.equals(userId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public NotificationType getType() {
        return type;
    }

    public UUID getPostId() {
        return postId;
    }

    public UUID getCommentId() {
        return commentId;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
