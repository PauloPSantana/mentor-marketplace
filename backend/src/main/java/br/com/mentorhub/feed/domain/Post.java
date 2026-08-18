package br.com.mentorhub.feed.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Post {

    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final int MAX_IMAGE_URL_LENGTH = 500;

    private final UUID id;
    private final UUID authorUserId;
    private String content;
    private String imageUrl;
    private final String authorName;
    private final String authorPhotoUrl;
    private final String authorHeadline;
    private final String authorRole;
    private final Instant createdAt;
    private Instant updatedAt;

    private Post(
            UUID id,
            UUID authorUserId,
            String content,
            String imageUrl,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.authorUserId = Objects.requireNonNull(authorUserId);
        this.content = requireContent(content);
        this.imageUrl = normalizeImageUrl(imageUrl);
        this.authorName = requireAuthorName(authorName);
        this.authorPhotoUrl = blankToNull(authorPhotoUrl);
        this.authorHeadline = blankToNull(authorHeadline);
        this.authorRole = Objects.requireNonNull(authorRole);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Post publish(
            UUID authorUserId,
            String content,
            String imageUrl,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole
    ) {
        Instant now = Instant.now();
        return new Post(
                UUID.randomUUID(),
                authorUserId,
                content,
                imageUrl,
                authorName,
                authorPhotoUrl,
                authorHeadline,
                authorRole,
                now,
                now
        );
    }

    public static Post restore(
            UUID id,
            UUID authorUserId,
            String content,
            String imageUrl,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole,
            Instant createdAt,
            Instant updatedAt
    ) {
        Instant restoredUpdatedAt = updatedAt != null ? updatedAt : createdAt;
        return new Post(
                id,
                authorUserId,
                content,
                imageUrl,
                authorName,
                authorPhotoUrl,
                authorHeadline,
                authorRole,
                createdAt,
                restoredUpdatedAt
        );
    }

    public void update(String content, String imageUrl) {
        this.content = requireContent(content);
        this.imageUrl = normalizeImageUrl(imageUrl);
        this.updatedAt = Instant.now();
    }

    public boolean isOwnedBy(UUID userId) {
        return authorUserId.equals(userId);
    }

    private static String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("INVALID_POST", "O conteúdo da publicação é obrigatório");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("INVALID_POST", "Conteúdo deve ter no máximo 5000 caracteres");
        }
        return trimmed;
    }

    private static String requireAuthorName(String authorName) {
        if (authorName == null || authorName.isBlank()) {
            throw new BusinessException("INVALID_POST", "Nome do autor é obrigatório");
        }
        return authorName.trim();
    }

    private static String normalizeImageUrl(String imageUrl) {
        String normalized = blankToNull(imageUrl);
        if (normalized != null && normalized.length() > MAX_IMAGE_URL_LENGTH) {
            throw new BusinessException("INVALID_POST", "URL da imagem deve ter no máximo 500 caracteres");
        }
        return normalized;
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

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
