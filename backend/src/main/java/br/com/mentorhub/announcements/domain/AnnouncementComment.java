package br.com.mentorhub.announcements.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AnnouncementComment {

    public static final String DELETED_PLACEHOLDER = "[Comentário removido]";
    private static final int MAX_CONTENT_LENGTH = 1000;

    private final UUID id;
    private final UUID announcementId;
    private final UUID parentCommentId;
    private final UUID authorUserId;
    private final String content;
    private final String authorName;
    private final String authorPhotoUrl;
    private final String authorRole;
    private final AnnouncementCommentStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private AnnouncementComment(
            UUID id,
            UUID announcementId,
            UUID parentCommentId,
            UUID authorUserId,
            String content,
            String authorName,
            String authorPhotoUrl,
            String authorRole,
            AnnouncementCommentStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.announcementId = Objects.requireNonNull(announcementId);
        this.parentCommentId = parentCommentId;
        this.authorUserId = Objects.requireNonNull(authorUserId);
        this.content = resolveContent(content, status);
        this.authorName = requireAuthorName(authorName);
        this.authorPhotoUrl = blankToNull(authorPhotoUrl);
        this.authorRole = Objects.requireNonNull(authorRole);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static AnnouncementComment create(
            UUID announcementId,
            UUID parentCommentId,
            UUID authorUserId,
            String content,
            String authorName,
            String authorPhotoUrl,
            String authorRole
    ) {
        Instant now = Instant.now();
        return new AnnouncementComment(
                UUID.randomUUID(),
                announcementId,
                parentCommentId,
                authorUserId,
                content,
                authorName,
                authorPhotoUrl,
                authorRole,
                AnnouncementCommentStatus.ACTIVE,
                now,
                now
        );
    }

    public static AnnouncementComment restore(
            UUID id,
            UUID announcementId,
            UUID parentCommentId,
            UUID authorUserId,
            String content,
            String authorName,
            String authorPhotoUrl,
            String authorRole,
            AnnouncementCommentStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new AnnouncementComment(
                id,
                announcementId,
                parentCommentId,
                authorUserId,
                content,
                authorName,
                authorPhotoUrl,
                authorRole,
                status,
                createdAt,
                updatedAt
        );
    }

    public AnnouncementComment markAsDeleted() {
        if (status == AnnouncementCommentStatus.DELETED) {
            return this;
        }
        return restore(
                id,
                announcementId,
                parentCommentId,
                authorUserId,
                content,
                authorName,
                authorPhotoUrl,
                authorRole,
                AnnouncementCommentStatus.DELETED,
                createdAt,
                Instant.now()
        );
    }

    public boolean isOwnedBy(UUID userId) {
        return authorUserId.equals(userId);
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public boolean isActive() {
        return status == AnnouncementCommentStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return status == AnnouncementCommentStatus.DELETED;
    }

    public String getDisplayContent() {
        return isDeleted() ? DELETED_PLACEHOLDER : content;
    }

    private static String resolveContent(String content, AnnouncementCommentStatus status) {
        if (status == AnnouncementCommentStatus.DELETED) {
            return DELETED_PLACEHOLDER;
        }
        return requireContent(content);
    }

    private static String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("INVALID_COMMENT", "O comentário é obrigatório");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("INVALID_COMMENT", "Comentário deve ter no máximo 1000 caracteres");
        }
        return trimmed;
    }

    private static String requireAuthorName(String authorName) {
        if (authorName == null || authorName.isBlank()) {
            throw new BusinessException("INVALID_COMMENT", "Nome do autor é obrigatório");
        }
        return authorName.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAnnouncementId() {
        return announcementId;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorPhotoUrl() {
        return authorPhotoUrl;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public AnnouncementCommentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
