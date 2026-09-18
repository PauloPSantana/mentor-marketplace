package br.com.mentorhub.announcements.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Announcement {

    private static final int MAX_TITLE = 180;
    private static final int MAX_BODY = 5000;

    private final UUID id;
    private final UUID groupId;
    private final UUID authorUserId;
    private final String title;
    private final String body;
    private final AnnouncementAudienceType audienceType;
    private final List<UUID> recipientUserIds;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Announcement(
            UUID id,
            UUID groupId,
            UUID authorUserId,
            String title,
            String body,
            AnnouncementAudienceType audienceType,
            List<UUID> recipientUserIds,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.groupId = Objects.requireNonNull(groupId);
        this.authorUserId = Objects.requireNonNull(authorUserId);
        this.title = normalizeTitle(title);
        this.body = requireBody(body);
        this.audienceType = Objects.requireNonNull(audienceType);
        this.recipientUserIds = List.copyOf(recipientUserIds);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        validateAudience();
    }

    public static Announcement publish(
            UUID groupId,
            UUID authorUserId,
            String title,
            String body,
            AnnouncementAudienceType audienceType,
            List<UUID> recipientUserIds
    ) {
        Instant now = Instant.now();
        return new Announcement(
                UUID.randomUUID(),
                groupId,
                authorUserId,
                title,
                body,
                audienceType,
                recipientUserIds == null ? List.of() : recipientUserIds,
                now,
                now
        );
    }

    public static Announcement restore(
            UUID id,
            UUID groupId,
            UUID authorUserId,
            String title,
            String body,
            AnnouncementAudienceType audienceType,
            List<UUID> recipientUserIds,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Announcement(
                id,
                groupId,
                authorUserId,
                title,
                body,
                audienceType,
                recipientUserIds == null ? List.of() : recipientUserIds,
                createdAt,
                updatedAt
        );
    }

    public boolean isVisibleTo(UUID userId) {
        if (userId.equals(authorUserId)) {
            return true;
        }
        if (audienceType == AnnouncementAudienceType.GROUP) {
            return true;
        }
        return recipientUserIds.contains(userId);
    }

    public boolean isOwnedBy(UUID userId) {
        return authorUserId.equals(userId);
    }

    private void validateAudience() {
        if (audienceType == AnnouncementAudienceType.GROUP) {
            return;
        }
        Set<UUID> unique = new LinkedHashSet<>(recipientUserIds);
        unique.remove(null);
        unique.remove(authorUserId);
        if (unique.isEmpty()) {
            throw new BusinessException(
                    "INVALID_ANNOUNCEMENT_AUDIENCE",
                    "Selecione pelo menos um mentorado para este comunicado"
            );
        }
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        String trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE) {
            throw new BusinessException("INVALID_ANNOUNCEMENT_TITLE", "Título deve ter no máximo 180 caracteres");
        }
        return trimmed;
    }

    private static String requireBody(String body) {
        if (body == null || body.isBlank()) {
            throw new BusinessException("INVALID_ANNOUNCEMENT", "O comunicado é obrigatório");
        }
        String trimmed = body.trim();
        if (trimmed.length() > MAX_BODY) {
            throw new BusinessException("INVALID_ANNOUNCEMENT", "Comunicado deve ter no máximo 5000 caracteres");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public AnnouncementAudienceType getAudienceType() {
        return audienceType;
    }

    public List<UUID> getRecipientUserIds() {
        return recipientUserIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
