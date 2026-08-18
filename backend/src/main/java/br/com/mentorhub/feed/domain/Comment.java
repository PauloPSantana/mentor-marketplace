package br.com.mentorhub.feed.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Comment {

    public static final String DELETED_PLACEHOLDER = "[Comentário removido]";
    private static final int MAX_CONTENT_LENGTH = 1000;

    private final UUID id;
    private final UUID postId;
    private final UUID parentCommentId;
    private final UUID authorUserId;
    private final String content;
    private final String authorName;
    private final String authorPhotoUrl;
    private final String authorHeadline;
    private final String authorRole;
    private final CommentStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Comment(
            UUID id,
            UUID postId,
            UUID parentCommentId,
            UUID authorUserId,
            String content,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole,
            CommentStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.postId = Objects.requireNonNull(postId);
        this.parentCommentId = parentCommentId;
        this.authorUserId = Objects.requireNonNull(authorUserId);
        this.content = resolveContent(content, status);
        this.authorName = requireAuthorName(authorName);
        this.authorPhotoUrl = blankToNull(authorPhotoUrl);
        this.authorHeadline = blankToNull(authorHeadline);
        this.authorRole = Objects.requireNonNull(authorRole);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = updatedAt;
    }

    public static Comment create(
            UUID postId,
            UUID parentCommentId,
            UUID authorUserId,
            String content,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole
    ) {
        Instant now = Instant.now();
        return new Comment(
                UUID.randomUUID(),
                postId,
                parentCommentId,
                authorUserId,
                content,
                authorName,
                authorPhotoUrl,
                authorHeadline,
                authorRole,
                CommentStatus.ACTIVE,
                now,
                now
        );
    }

    public static Comment restore(
            UUID id,
            UUID postId,
            UUID parentCommentId,
            UUID authorUserId,
            String content,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole,
            CommentStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Comment(
                id,
                postId,
                parentCommentId,
                authorUserId,
                content,
                authorName,
                authorPhotoUrl,
                authorHeadline,
                authorRole,
                status,
                createdAt,
                updatedAt
        );
    }

    public Comment markAsDeleted() {
        if (status == CommentStatus.DELETED) {
            return this;
        }
        Instant now = Instant.now();
        return new Comment(
                id,
                postId,
                parentCommentId,
                authorUserId,
                content,
                authorName,
                authorPhotoUrl,
                authorHeadline,
                authorRole,
                CommentStatus.DELETED,
                createdAt,
                now
        );
    }

    public boolean isOwnedBy(UUID userId) {
        return authorUserId.equals(userId);
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public boolean isActive() {
        return status == CommentStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return status == CommentStatus.DELETED;
    }

    public String getDisplayContent() {
        return isDeleted() ? DELETED_PLACEHOLDER : content;
    }

    private static String resolveContent(String content, CommentStatus status) {
        if (status == CommentStatus.DELETED) {
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

    public UUID getPostId() {
        return postId;
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

    public String getAuthorHeadline() {
        return authorHeadline;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
