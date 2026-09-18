package br.com.mentorhub.institutions.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Institution {

    private final UUID id;
    private String name;
    private final UUID ownerUserId;
    private final InstitutionStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Institution(
            UUID id,
            String name,
            UUID ownerUserId,
            InstitutionStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.ownerUserId = Objects.requireNonNull(ownerUserId);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Institution create(UUID ownerUserId, String name) {
        Instant now = Instant.now();
        return new Institution(UUID.randomUUID(), name, ownerUserId, InstitutionStatus.ACTIVE, now, now);
    }

    public static Institution restore(
            UUID id,
            String name,
            UUID ownerUserId,
            InstitutionStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Institution(id, name, ownerUserId, status, createdAt, updatedAt);
    }

    public void rename(String name) {
        this.name = requireName(name);
        this.updatedAt = Instant.now();
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerUserId.equals(userId);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public InstitutionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("INVALID_INSTITUTION", "Nome da instituição é obrigatório");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 160) {
            throw new BusinessException("INVALID_INSTITUTION", "Nome da instituição deve ter no máximo 160 caracteres");
        }
        return trimmed;
    }
}
