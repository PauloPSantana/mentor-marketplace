package br.com.mentorhub.feed.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Comment {

    private static final int MAX_CONTENT_LENGTH = 2000;

    private final UUID id;
    private final UUID postId;
    private final UUID parentCommentId;
    private final UUID authorUserId;
    private final String content;
    private final String authorName;
    private final String authorPhotoUrl;
    private final String authorHeadline;
    private final String authorRole;
    private final Instant createdAt;

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
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.postId = Objects.requireNonNull(postId);
        this.parentCommentId = parentCommentId;
        this.authorUserId = Objects.requireNonNull(authorUserId);
        this.content = requireContent(content);
        this.authorName = requireAuthorName(authorName);
        this.authorPhotoUrl = blankToNull(authorPhotoUrl);
        this.authorHeadline = blankToNull(authorHeadline);
        this.authorRole = Objects.requireNonNull(authorRole);
        this.createdAt = Objects.requireNonNull(createdAt);
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
                Instant.now()
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
            Instant createdAt
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
                createdAt
        );
    }

    public boolean isOwnedBy(UUID userId) {
        return authorUserId.equals(userId);
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    private static String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("INVALID_COMMENT", "O comentário é obrigatório");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("INVALID_COMMENT", "Comentário deve ter no máximo 2000 caracteres");
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
