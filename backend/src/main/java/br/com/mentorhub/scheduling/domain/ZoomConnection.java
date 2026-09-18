package br.com.mentorhub.scheduling.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ZoomConnection {

    private static final int TOKEN_TTL_SKEW_SECONDS = 60;

    private final UUID userId;
    private final String zoomUserId;
    private final String zoomEmail;
    private final String accessToken;
    private final String refreshToken;
    private final Instant expiresAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ZoomConnection(
            UUID userId,
            String zoomUserId,
            String zoomEmail,
            String accessToken,
            String refreshToken,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.userId = Objects.requireNonNull(userId);
        this.zoomUserId = requireText(zoomUserId);
        this.zoomEmail = blankToNull(zoomEmail);
        this.accessToken = requireText(accessToken);
        this.refreshToken = refreshToken == null ? "" : refreshToken.trim();
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static ZoomConnection connect(
            UUID userId,
            String zoomUserId,
            String zoomEmail,
            String accessToken,
            String refreshToken,
            Instant expiresAt
    ) {
        Instant now = Instant.now();
        return new ZoomConnection(userId, zoomUserId, zoomEmail, accessToken, refreshToken, expiresAt, now, now);
    }

    public static ZoomConnection restore(
            UUID userId,
            String zoomUserId,
            String zoomEmail,
            String accessToken,
            String refreshToken,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new ZoomConnection(
                userId,
                zoomUserId,
                zoomEmail,
                accessToken,
                refreshToken,
                expiresAt,
                createdAt,
                updatedAt
        );
    }

    public ZoomConnection refresh(String accessToken, String refreshToken, Instant expiresAt) {
        return new ZoomConnection(
                userId,
                zoomUserId,
                zoomEmail,
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

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Token Zoom inválido");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getZoomUserId() {
        return zoomUserId;
    }

    public String getZoomEmail() {
        return zoomEmail;
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
