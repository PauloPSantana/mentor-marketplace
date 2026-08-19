package br.com.mentorhub.identity.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class User {

    private final UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private UserRole role;
    private UserStatus status;
    private String photoUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            UUID id,
            String name,
            String email,
            String passwordHash,
            UserRole role,
            UserStatus status,
            String photoUrl,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.email = requireEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.photoUrl = normalizePhotoUrl(photoUrl);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static User register(String name, String email, String passwordHash, UserRole role) {
        if (role == UserRole.ADMIN) {
            throw new BusinessException("INVALID_ROLE", "Cadastro público não permite papel ADMIN");
        }
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), name, email, passwordHash, role, UserStatus.ACTIVE, null, now, now);
    }

    public static User restore(
            UUID id,
            String name,
            String email,
            String passwordHash,
            UserRole role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return restore(id, name, email, passwordHash, role, status, createdAt, updatedAt, null);
    }

    public static User restore(
            UUID id,
            String name,
            String email,
            String passwordHash,
            UserRole role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt,
            String photoUrl
    ) {
        return new User(id, name, email, passwordHash, role, status, photoUrl, createdAt, updatedAt);
    }

    public void rename(String name) {
        this.name = requireName(name);
        this.updatedAt = Instant.now();
    }

    public void updatePhoto(String photoUrl) {
        this.photoUrl = requirePhotoUrl(photoUrl);
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("INVALID_NAME", "Nome é obrigatório");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 120) {
            throw new BusinessException("INVALID_NAME", "Nome deve ter no máximo 120 caracteres");
        }
        return trimmed;
    }

    private static String requireEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("INVALID_EMAIL", "Email é obrigatório");
        }
        return email.trim().toLowerCase();
    }

    private static String requirePhotoUrl(String photoUrl) {
        String normalized = normalizePhotoUrl(photoUrl);
        if (normalized == null) {
            throw new BusinessException("INVALID_PHOTO", "Foto é obrigatória");
        }
        return normalized;
    }

    private static String normalizePhotoUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return null;
        }
        String trimmed = photoUrl.trim();
        if (trimmed.length() > 500) {
            throw new BusinessException("INVALID_PHOTO", "URL da foto deve ter no máximo 500 caracteres");
        }
        return trimmed;
    }
}
