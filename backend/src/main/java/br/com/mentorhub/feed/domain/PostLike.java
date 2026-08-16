package br.com.mentorhub.feed.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PostLike {

    private final UUID id;
    private final UUID postId;
    private final UUID userId;
    private final Instant createdAt;

    private PostLike(UUID id, UUID postId, UUID userId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.postId = Objects.requireNonNull(postId);
        this.userId = Objects.requireNonNull(userId);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static PostLike create(UUID postId, UUID userId) {
        return new PostLike(UUID.randomUUID(), postId, userId, Instant.now());
    }

    public static PostLike restore(UUID id, UUID postId, UUID userId, Instant createdAt) {
        return new PostLike(id, postId, userId, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
