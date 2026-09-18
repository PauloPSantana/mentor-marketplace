package br.com.mentorhub.scheduling.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class GoogleConnection {

    private static final int TOKEN_TTL_SKEW_SECONDS = 60;

    private final UUID userId;
    private final String googleUserId;
    private final String googleEmail;
    private final String accessToken;
    private final String refreshToken;
    private final Instant expiresAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private GoogleConnection(
            UUID userId,
            String googleUserId,
            String googleEmail,
            String accessToken,
            String refreshToken,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.userId = Objects.requireNonNull(userId);
        this.googleUserId = requireText(googleUserId, "Conta Google inválida");
        this.googleEmail = blankToNull(googleEmail);
        this.accessToken = requireText(accessToken, "Token Google inválido");
        this.refreshToken = refreshToken == null ? "" : refreshToken.trim();
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static GoogleConnection connect(
            UUID userId,
            String googleUserId,
            String googleEmail,
            String accessToken,
            String refreshToken,
            Instant expiresAt
    ) {
        Instant now = Instant.now();
        return new GoogleConnection(userId, googleUserId, googleEmail, accessToken, refreshToken, expiresAt, now, now);
    }

    public static GoogleConnection restore(
            UUID userId,
            String googleUserId,
            String googleEmail,
            String accessToken,
            String refreshToken,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new GoogleConnection(
                userId, googleUserId, googleEmail, accessToken, refreshToken, expiresAt, createdAt, updatedAt
        );
    }

    public GoogleConnection refresh(String accessToken, String refreshToken, Instant expiresAt) {
        return new GoogleConnection(
                userId,
                googleUserId,
                googleEmail,
                accessToken,
                refreshToken == null || refreshToken.isBlank() ? this.refreshToken : refreshToken,
                expiresAt,
                createdAt,
                Instant.now()
        );
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.minusSeconds(TOKEN_TTL_SKEW_SECONDS).isAfter(now);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getGoogleUserId() {
        return googleUserId;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
