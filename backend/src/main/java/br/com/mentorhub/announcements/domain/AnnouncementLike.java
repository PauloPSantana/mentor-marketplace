package br.com.mentorhub.announcements.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AnnouncementLike {

    private final UUID id;
    private final UUID announcementId;
    private final UUID userId;
    private final Instant createdAt;

    private AnnouncementLike(UUID id, UUID announcementId, UUID userId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.announcementId = Objects.requireNonNull(announcementId);
        this.userId = Objects.requireNonNull(userId);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static AnnouncementLike create(UUID announcementId, UUID userId) {
        return new AnnouncementLike(UUID.randomUUID(), announcementId, userId, Instant.now());
    }

    public static AnnouncementLike restore(UUID id, UUID announcementId, UUID userId, Instant createdAt) {
        return new AnnouncementLike(id, announcementId, userId, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getAnnouncementId() {
        return announcementId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
