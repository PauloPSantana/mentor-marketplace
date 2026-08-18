package br.com.mentorhub.social.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class UserBlock {

    private final UUID id;
    private final UUID blockerId;
    private final UUID blockedId;
    private final Instant createdAt;

    private UserBlock(UUID id, UUID blockerId, UUID blockedId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.blockerId = Objects.requireNonNull(blockerId);
        this.blockedId = Objects.requireNonNull(blockedId);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static UserBlock create(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("Usuário não pode bloquear a si mesmo");
        }
        return new UserBlock(UUID.randomUUID(), blockerId, blockedId, Instant.now());
    }

    public static UserBlock restore(UUID id, UUID blockerId, UUID blockedId, Instant createdAt) {
        return new UserBlock(id, blockerId, blockedId, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getBlockerId() {
        return blockerId;
    }

    public UUID getBlockedId() {
        return blockedId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
