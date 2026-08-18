package br.com.mentorhub.social.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class UserFollow {

    private final UUID id;
    private final UUID followerId;
    private final UUID followedId;
    private final Instant createdAt;

    private UserFollow(UUID id, UUID followerId, UUID followedId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.followerId = Objects.requireNonNull(followerId);
        this.followedId = Objects.requireNonNull(followedId);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static UserFollow create(UUID followerId, UUID followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Usuário não pode seguir a si mesmo");
        }
        return new UserFollow(UUID.randomUUID(), followerId, followedId, Instant.now());
    }

    public static UserFollow restore(UUID id, UUID followerId, UUID followedId, Instant createdAt) {
        return new UserFollow(id, followerId, followedId, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getFollowerId() {
        return followerId;
    }

    public UUID getFollowedId() {
        return followedId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
